package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Equipamento;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.EquipamentoRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EquipamentoService {

    private final EquipamentoRepository equipamentoRepository;

    public EquipamentoService(EquipamentoRepository equipamentoRepository) {
        this.equipamentoRepository = equipamentoRepository;
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

        if (equipamento.getSituacao() == null) {
            equipamento.setSituacao(StatusEquipamento.DISPONIVEL);
        }

        return equipamentoRepository.save(equipamento);
    }

    @Transactional
    public void marcarComoConferido(Long id) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado."));
        equipamento.setConferidoInventario(true);
        equipamentoRepository.save(equipamento);
    }

    @Transactional
    public void reiniciarCicloInventario() {
        List<Equipamento> todos = equipamentoRepository.findAll();
        todos.forEach(eq -> eq.setConferidoInventario(false));
        equipamentoRepository.saveAll(todos);
    }

    @Transactional
    public void excluir(Long id) {
        Equipamento equipamento = equipamentoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado."));

        if (equipamento.getSituacao() == StatusEquipamento.EMPRESTADO) {
            throw new RegraNegocioException("Não é possível excluir um equipamento que está atualmente emprestado (em cautela).");
        }

        try {
            equipamentoRepository.deleteById(id);
            equipamentoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("Não é possível excluir este equipamento pois ele possui histórico de movimentações patrimoniais.");
        }
    }
}