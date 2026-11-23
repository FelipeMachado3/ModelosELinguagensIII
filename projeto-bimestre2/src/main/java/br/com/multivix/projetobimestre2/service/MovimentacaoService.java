package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Equipamento;
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

    public MovimentacaoService(MovimentacaoRepository movimentacaoRepository,
                               EquipamentoRepository equipamentoRepository) {
        this.movimentacaoRepository = movimentacaoRepository;
        this.equipamentoRepository = equipamentoRepository;
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

        Equipamento equipamento = equipamentoRepository.findById(movimentacao.getEquipamento().getId())
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado no cadastro patrimonial."));

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

        equipamento.setSituacao(StatusEquipamento.EMPRESTADO);
        equipamentoRepository.save(equipamento);

        Movimentacao salva = movimentacaoRepository.save(movimentacao);

        if (salva.getNumeroTermo() == null || salva.getNumeroTermo().isBlank()) {
            String ano = String.valueOf(salva.getDataRetirada().getYear());
            salva.setNumeroTermo(String.format("TC-%s-%04d", ano, salva.getId()));
            salva = movimentacaoRepository.save(salva);
        }

        return salva;
    }

    @Transactional
    public Movimentacao registrarDevolucao(Long movimentacaoId, LocalDate dataDevolucao, String observacaoDevolucao) {
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

        movimentacao.setDataDevolucao(dataEfetiva);

        if (observacaoDevolucao != null && !observacaoDevolucao.isBlank()) {
            String obsAtual = (movimentacao.getObservacao() != null && !movimentacao.getObservacao().isBlank())
                    ? movimentacao.getObservacao() + " | Devolução: " + observacaoDevolucao.trim()
                    : "Devolução: " + observacaoDevolucao.trim();
            movimentacao.setObservacao(obsAtual.length() > 255 ? obsAtual.substring(0, 255) : obsAtual);
        }

        Equipamento equipamento = movimentacao.getEquipamento();
        equipamento.setSituacao(StatusEquipamento.DISPONIVEL);
        equipamentoRepository.save(equipamento);

        return movimentacaoRepository.save(movimentacao);
    }

    @Transactional
    public void excluir(Long id) {
        Movimentacao movimentacao = movimentacaoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Movimentação não encontrada."));

        if (movimentacao.isAtiva()) {
            throw new RegraNegocioException("Não é possível excluir uma cautela em aberto. Registre a devolução primeiro.");
        }

        movimentacaoRepository.deleteById(id);
    }
}
