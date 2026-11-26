package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Equipamento;
import br.com.multivix.projetobimestre2.entity.EstadoConservacao;
import br.com.multivix.projetobimestre2.entity.Movimentacao;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.EquipamentoRepository;
import br.com.multivix.projetobimestre2.repository.MovimentacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class MovimentacaoService {

    private final MovimentacaoRepository movimentacaoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final AuditoriaService auditoriaService;
    private final AnexoService anexoService;

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               EquipamentoRepository equipamentoRepository,
                               AuditoriaService auditoriaService,
                               AnexoService anexoService) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.auditoriaService = auditoriaService;
        this.anexoService = anexoService;
    }

    public List<Movimentacao> listarTodas() {
        return movimentacaoRepository.findAllByOrderByDataRetiradaDescIdDesc();
    }

    public List<Movimentacao> listarAtivas() {
        return movimentacaoRepository.findByDataDevolucaoIsNullOrderByDataPrevistaDevolucaoAsc();
    }

    public List<Movimentacao> listarPorSetor(Long setorId) {
        return movimentacaoRepository.findByResponsavelSetorIdOrderByDataRetiradaDescIdDesc(setorId);
    }

    public List<Movimentacao> listarAtivasPorSetor(Long setorId) {
        return movimentacaoRepository.findByResponsavelSetorIdAndDataDevolucaoIsNullOrderByDataPrevistaDevolucaoAsc(setorId);
    }

    public Optional<Movimentacao> buscarPorId(Long id) {
        return movimentacaoRepository.findById(id);
    }

    public long contarAtivas() {
        return movimentacaoRepository.countByDataDevolucaoIsNull();
    }

    public long contarAtrasadas() {
        return movimentacaoRepository.countByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(LocalDate.now());
    }

    @Transactional
    public Movimentacao registrarEmprestimo(Movimentacao movimentacao) {
        if (movimentacao.getEquipamento() == null || movimentacao.getEquipamento().getId() == null) {
            throw new RegraNegocioException("Selecione um equipamento válido para a cautela.");
        }
        if (movimentacao.getResponsavel() != null && movimentacao.getResponsavel().getId() != null) {
            boolean possuiAtraso = movimentacaoRepository
                    .existsByResponsavelIdAndDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(
                            movimentacao.getResponsavel().getId(), LocalDate.now());
            if (possuiAtraso) {
                throw new RegraNegocioException(
                        "Solicitante possui cautela em atraso. Novas retiradas ficam bloqueadas até a regularização."
                );
            }
        }

        Equipamento equipamento = equipamentoRepository.findById(movimentacao.getEquipamento().getId())
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado no cadastro patrimonial."));

        if (equipamento.getSituacao() == StatusEquipamento.BAIXADO) {
            throw new RegraNegocioException("Equipamento baixado não pode ser movimentado.");
        }
        if (!equipamento.getSituacao().isDisponivelParaCautela()
                || movimentacaoRepository.existsByEquipamentoIdAndDataDevolucaoIsNull(equipamento.getId())) {
            throw new RegraNegocioException(
                    "O equipamento tombamento #" + equipamento.getPatrimonio()
                            + " não está disponível para retirada (Situação atual: " + equipamento.getSituacao().getDescricao() + ")."
            );
        }

        if (movimentacao.getDataRetirada() == null) {
            movimentacao.setDataRetirada(LocalDate.now());
        }

        if (movimentacao.getDataPrevistaDevolucao() == null) {
            throw new RegraNegocioException("Informe a data prevista de devolução do equipamento.");
        }

        if (movimentacao.getDataPrevistaDevolucao().isBefore(movimentacao.getDataRetirada())) {
            throw new RegraNegocioException("A data prevista de devolução não pode ser anterior à data de retirada.");
        }

        movimentacao.setEquipamento(equipamento);
        movimentacao.setDataDevolucao(null);
        movimentacao.setQuantidadeProrrogacoes(0);
        movimentacao.setDevolvidoComAvaria(false);

        equipamento.setSituacao(StatusEquipamento.EMPRESTADO);
        equipamentoRepository.save(equipamento);

        Movimentacao salva = movimentacaoRepository.save(movimentacao);

        if (salva.getNumeroTermo() == null || salva.getNumeroTermo().isBlank()) {
            String ano = String.valueOf(salva.getDataRetirada().getYear());
            salva.setNumeroTermo(String.format("TC-%s-%04d", ano, salva.getId()));
            salva = movimentacaoRepository.save(salva);
        }

        auditoriaService.registrar(
                "MOVIMENTACOES",
                "ABRIR_CAUTELA",
                "Movimentacao",
                salva.getId(),
                equipamento.getPatrimonio(),
                "Cautela " + salva.getNumeroTermo() + " aberta para patrimônio #" + equipamento.getPatrimonio() + "."
        );

        return salva;
    }

    @Transactional
    public Movimentacao prorrogarPrazo(Long movimentacaoId, LocalDate novaDataPrevista, String justificativa) {
        Movimentacao mov = movimentacaoRepository.findById(movimentacaoId)
                .orElseThrow(() -> new RegraNegocioException("Movimentação não encontrada."));

        if (!mov.isAtiva()) {
            throw new RegraNegocioException("Não é possível prorrogar uma cautela já encerrada.");
        }
        if (!mov.podeProrrogar()) {
            throw new RegraNegocioException(
                    "Limite de " + Movimentacao.MAX_PRORROGACOES + " prorrogações consecutivas atingido para esta cautela."
            );
        }
        if (justificativa == null || justificativa.trim().isEmpty()) {
            throw new RegraNegocioException("A justificativa é obrigatória para prorrogar o prazo da cautela.");
        }
        if (novaDataPrevista == null || !novaDataPrevista.isAfter(mov.getDataPrevistaDevolucao())) {
            throw new RegraNegocioException("A nova data deve ser posterior à data prevista atual.");
        }

        mov.setDataPrevistaDevolucao(novaDataPrevista);
        mov.setQuantidadeProrrogacoes(mov.getQuantidadeProrrogacoes() + 1);

        String obsAtual = mov.getObservacao() != null ? mov.getObservacao() + " | " : "";
        String novaObs = obsAtual + "Prorrogação " + mov.getQuantidadeProrrogacoes() + "/" + Movimentacao.MAX_PRORROGACOES
                + " até " + novaDataPrevista + " (" + justificativa.trim() + ")";
        mov.setObservacao(novaObs.length() > 500 ? novaObs.substring(0, 500) : novaObs);

        Movimentacao salva = movimentacaoRepository.save(mov);
        String pat = salva.getEquipamento() != null ? salva.getEquipamento().getPatrimonio() : null;
        auditoriaService.registrar(
                "MOVIMENTACOES",
                "PRORROGAR_CAUTELA",
                "Movimentacao",
                salva.getId(),
                pat,
                "Termo " + salva.getNumeroTermo() + " prorrogado até " + novaDataPrevista + ". Justificativa: " + justificativa.trim()
        );
        return salva;
    }

    @Transactional
    public Movimentacao registrarDevolucao(Long movimentacaoId, LocalDate dataDevolucao, String observacaoDevolucao) {
        return registrarDevolucaoCompleta(movimentacaoId, dataDevolucao, observacaoDevolucao, null, false);
    }

    @Transactional
    public Movimentacao registrarDevolucaoCompleta(Long movimentacaoId,
                                                   LocalDate dataDevolucao,
                                                   String observacaoDevolucao,
                                                   EstadoConservacao estadoConservacao,
                                                   boolean avariaInformada) {
        Movimentacao movimentacao = movimentacaoRepository.findById(movimentacaoId)
                .orElseThrow(() -> new RegraNegocioException("Termo de movimentação não encontrado."));

        if (!movimentacao.isAtiva()) {
            throw new RegraNegocioException("Este termo de cautela já foi encerrado anteriormente.");
        }

        LocalDate dataEfetiva = (dataDevolucao != null) ? dataDevolucao : LocalDate.now();
        if (dataEfetiva.isBefore(movimentacao.getDataRetirada())) {
            throw new RegraNegocioException("A data de devolução não pode ser anterior à data de retirada ("
                    + movimentacao.getDataRetirada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ").");
        }

        boolean comAvaria = avariaInformada
                || estadoConservacao == EstadoConservacao.RUIM
                || estadoConservacao == EstadoConservacao.PESSIMO;

        if (comAvaria) {
            if (observacaoDevolucao == null || observacaoDevolucao.trim().isEmpty()) {
                throw new RegraNegocioException("Devolução com avaria exige descrição detalhada do dano/defeito.");
            }
            if (!anexoService.possuiEvidenciaAvariaNaMovimentacao(movimentacaoId)) {
                throw new RegraNegocioException("Devolução com avaria exige ao menos um anexo de evidência fotográfica ou relatório técnico.");
            }
        }

        movimentacao.setDataDevolucao(dataEfetiva);
        movimentacao.setDevolvidoComAvaria(comAvaria);

        if (observacaoDevolucao != null && !observacaoDevolucao.isBlank()) {
            String obsAtual = (movimentacao.getObservacao() != null && !movimentacao.getObservacao().isBlank())
                    ? movimentacao.getObservacao() + " | Devolução: " + observacaoDevolucao.trim()
                    : "Devolução: " + observacaoDevolucao.trim();
            movimentacao.setObservacao(obsAtual.length() > 500 ? obsAtual.substring(0, 500) : obsAtual);
        }

        Equipamento equipamento = movimentacao.getEquipamento();
        if (estadoConservacao != null) {
            equipamento.setEstadoConservacao(estadoConservacao);
        }
        equipamento.setSituacao(comAvaria ? StatusEquipamento.MANUTENCAO : StatusEquipamento.DISPONIVEL);
        equipamentoRepository.save(equipamento);

        Movimentacao salva = movimentacaoRepository.save(movimentacao);
        auditoriaService.registrar(
                "MOVIMENTACOES",
                comAvaria ? "DEVOLVER_COM_AVARIA" : "DEVOLVER_CAUTELA",
                "Movimentacao",
                salva.getId(),
                equipamento.getPatrimonio(),
                "Devolução do termo " + salva.getNumeroTermo() + " em " + dataEfetiva
                        + (comAvaria ? " (COM AVARIA -> Em Manutenção)." : " (Sem avarias -> Disponível).")
        );
        return salva;
    }

    @Transactional
    public void excluir(Long id) {
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Movimentação não encontrada."));

        if (movimentacao.isAtiva()) {
            throw new RegraNegocioException("Não é possível excluir uma cautela em aberto. Registre a devolução primeiro.");
        }

        String termo = movimentacao.getNumeroTermo();
        String pat = movimentacao.getEquipamento() != null ? movimentacao.getEquipamento().getPatrimonio() : null;
        movimentacaoRepository.deleteById(id);
        auditoriaService.registrar("MOVIMENTACOES", "EXCLUIR_MOVIMENTACAO", "Movimentacao", id, pat, "Registro histórico do termo " + termo + " excluído.");
    }
}