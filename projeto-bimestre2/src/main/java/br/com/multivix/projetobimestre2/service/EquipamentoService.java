package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Equipamento;
import br.com.multivix.projetobimestre2.entity.Setor;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.EquipamentoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;
    private final AuditoriaService auditoriaService;
    private final AnexoService anexoService;

    public EquipamentoService(EquipamentoRepository equipamentoRepository,
                              AuditoriaService auditoriaService,
                              AnexoService anexoService) {
        this.equipamentoRepository = equipamentoRepository;
        this.auditoriaService = auditoriaService;
        this.anexoService = anexoService;
    }

    public List<Equipamento> listarTodos() {
        List<Equipamento> lista = equipamentoRepository.findAllByOrderByPatrimonioAsc();
        return lista != null ? lista : List.of();
    }

    public List<Equipamento> listarDisponiveis() {
        List<Equipamento> lista = equipamentoRepository.findBySituacaoOrderByNomeAsc(StatusEquipamento.DISPONIVEL);
        return lista != null ? lista : List.of();
    }

    public List<Equipamento> listarPorSituacao(StatusEquipamento situacao) {
        if (situacao == null) {
            return listarTodos();
        }
        List<Equipamento> lista = equipamentoRepository.findBySituacaoOrderByNomeAsc(situacao);
        return lista != null ? lista : List.of();
    }

    public List<Equipamento> listarPendentesInventario() {
        List<Equipamento> lista = equipamentoRepository.findByConferidoInventarioFalseOrderByPatrimonioAsc();
        return lista != null ? lista : List.of();
    }

    public Optional<Equipamento> buscarPorId(Long id) {
        return equipamentoRepository.findById(id);
    }

    public long contarTotal() {
        return equipamentoRepository.count();
    }

    public long contarPorSituacao(StatusEquipamento situacao) {
        return equipamentoRepository.countBySituacao(situacao);
    }

    public long contarPendentesInventario() {
        return equipamentoRepository.countByConferidoInventarioFalse();
    }

    @Transactional
    public Equipamento salvar(Equipamento equipamento) {
        String tombamentoNormalizado = equipamento.getPatrimonio() != null
                ? equipamento.getPatrimonio().trim().toUpperCase()
                : "";
        equipamento.setPatrimonio(tombamentoNormalizado);

        boolean patrimonioDuplicado = (equipamento.getId() == null)
                ? equipamentoRepository.existsByPatrimonioIgnoreCase(tombamentoNormalizado)
                : equipamentoRepository.existsByPatrimonioIgnoreCaseAndIdNot(tombamentoNormalizado, equipamento.getId());

        if (patrimonioDuplicado) {
            throw new RegraNegocioException(
                    "Já existe um equipamento cadastrado com o número de tombamento/patrimônio '" + tombamentoNormalizado + "'."
            );
        }

        boolean novo = (equipamento.getId() == null);
        if (!novo) {
            Equipamento existente = equipamentoRepository.findById(equipamento.getId())
                    .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado."));

            if (existente.getSituacao() == StatusEquipamento.BAIXADO) {
                throw new RegraNegocioException("Equipamento baixado (inservível) não pode ter seus dados alterados.");
            }

            if (existente.getSituacao() == StatusEquipamento.EMPRESTADO) {
                if (!Objects.equals(existente.getPatrimonio(), tombamentoNormalizado)) {
                    throw new RegraNegocioException("Não é permitido alterar o número de tombamento enquanto o bem está emprestado.");
                }
                String serieExistente = existente.getNumeroSerie() != null ? existente.getNumeroSerie().trim() : "";
                String serieNova = equipamento.getNumeroSerie() != null ? equipamento.getNumeroSerie().trim() : "";
                if (!Objects.equals(serieExistente, serieNova)) {
                    throw new RegraNegocioException("Não é permitido alterar o número de série enquanto o bem está emprestado.");
                }
                Long setorExistenteId = existente.getSetor() != null ? existente.getSetor().getId() : null;
                Long setorNovoId = equipamento.getSetor() != null ? equipamento.getSetor().getId() : null;
                if (!Objects.equals(setorExistenteId, setorNovoId)) {
                    throw new RegraNegocioException("Não é permitido alterar o setor de origem enquanto o bem está emprestado.");
                }
            }

            Setor setorAtual = existente.getSetor();
            Long setorAtualId = setorAtual != null ? setorAtual.getId() : null;
            Long setorNovoId = equipamento.getSetor() != null ? equipamento.getSetor().getId() : null;
            if (setorAtual != null && setorAtual.isInventarioAberto() && !Objects.equals(setorAtualId, setorNovoId)) {
                throw new RegraNegocioException(
                        "Transferência suspensa: o setor '" + setorAtual.getIdentificacao() + "' está com conferência de inventário aberta."
                );
            }
        }

        if (equipamento.getSituacao() == null) {
            equipamento.setSituacao(StatusEquipamento.DISPONIVEL);
        }

        Equipamento salvo = equipamentoRepository.save(equipamento);
        auditoriaService.registrar(
                "EQUIPAMENTOS",
                novo ? "CADASTRAR_EQUIPAMENTO" : "ATUALIZAR_EQUIPAMENTO",
                "Equipamento",
                salvo.getId(),
                salvo.getPatrimonio(),
                "Patrimônio #" + salvo.getPatrimonio() + " (" + salvo.getNome() + ") - Situação: " + salvo.getSituacao().name()
        );
        return salvo;
    }

    @Transactional
    public void marcarComoConferido(Long id) {
        marcarComoConferidoComDivergencia(id, "CONFORME", null);
    }

    @Transactional
    public void marcarComoConferidoComDivergencia(Long id, String statusDivergencia, String observacao) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado."));
        equipamento.setConferidoInventario(true);
        String divergenciaNorm = (statusDivergencia != null && !statusDivergencia.isBlank())
                ? statusDivergencia.trim().toUpperCase()
                : "CONFORME";
        equipamento.setStatusDivergenciaInventario(divergenciaNorm);
        if (observacao != null && !observacao.isBlank()) {
            equipamento.setObservacaoInventario(observacao.trim());
        }
        equipamentoRepository.save(equipamento);

        auditoriaService.registrar(
                "INVENTARIO",
                "CONFERIR_ITEM_INVENTARIO",
                "Equipamento",
                equipamento.getId(),
                equipamento.getPatrimonio(),
                "Item #" + equipamento.getPatrimonio() + " conferido (" + divergenciaNorm + ")"
                        + (observacao != null && !observacao.isBlank() ? ": " + observacao.trim() : ".")
        );
    }

    @Transactional
    public void reiniciarCicloInventario() {
        List<Equipamento> todos = equipamentoRepository.findAll();
        todos.forEach(eq -> {
            eq.setConferidoInventario(false);
            eq.setStatusDivergenciaInventario(null);
            eq.setObservacaoInventario(null);
        });
        equipamentoRepository.saveAll(todos);
        auditoriaService.registrar("INVENTARIO", "REINICIAR_INVENTARIO", "Ciclo geral de inventário reiniciado para todos os bens.");
    }

    @Transactional
    public void baixarPatrimonio(Long id, String motivoBaixa) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isManager = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));

        if (!isManager) {
            throw new RegraNegocioException("Apenas usuários com perfil MANAGER podem aprovar e efetivar a baixa patrimonial.");
        }
        if (motivoBaixa == null || motivoBaixa.trim().isEmpty()) {
            throw new RegraNegocioException("Informe a justificativa/motivo da baixa patrimonial.");
        }

        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado."));

        if (equipamento.getSituacao() == StatusEquipamento.EMPRESTADO) {
            throw new RegraNegocioException("Não é possível baixar um equipamento que está emprestado.");
        }
        if (equipamento.getSituacao() == StatusEquipamento.BAIXADO) {
            throw new RegraNegocioException("Este equipamento já se encontra baixado.");
        }
        if (!anexoService.possuiLaudoBaixaNoEquipamento(id)) {
            throw new RegraNegocioException("A baixa patrimonial exige ao menos um anexo do tipo LAUDO_BAIXA vinculado ao equipamento.");
        }

        equipamento.setSituacao(StatusEquipamento.BAIXADO);
        equipamento.setMotivoBaixa(motivoBaixa.trim());
        equipamento.setDataBaixa(LocalDate.now());
        equipamentoRepository.save(equipamento);

        auditoriaService.registrar(
                "EQUIPAMENTOS",
                "BAIXAR_PATRIMONIO",
                "Equipamento",
                equipamento.getId(),
                equipamento.getPatrimonio(),
                "Patrimônio #" + equipamento.getPatrimonio() + " baixado por MANAGER. Motivo: " + motivoBaixa.trim()
        );
    }

    @Transactional
    public void excluir(Long id) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado."));

        if (equipamento.getSituacao() == StatusEquipamento.EMPRESTADO) {
            throw new RegraNegocioException("Não é possível excluir um equipamento que está atualmente emprestado (em cautela).");
        }

        try {
            String pat = equipamento.getPatrimonio();
            equipamentoRepository.deleteById(id);
            equipamentoRepository.flush();
            auditoriaService.registrar("EQUIPAMENTOS", "EXCLUIR_EQUIPAMENTO", "Equipamento", id, pat, "Patrimônio #" + pat + " excluído.");
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("Não é possível excluir este equipamento pois ele possui histórico de movimentações patrimoniais.");
        }
    }
}