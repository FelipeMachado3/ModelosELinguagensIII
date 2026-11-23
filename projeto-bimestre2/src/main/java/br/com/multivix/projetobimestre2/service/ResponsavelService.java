package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Responsavel;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.ResponsavelRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;

    public ResponsavelService(ResponsavelRepository responsavelRepository) {
        this.responsavelRepository = responsavelRepository;
    }

    public List<Responsavel> listarTodos() {
        return responsavelRepository.findAllByOrderBySetorNomeAscNomeAsc();
    }

    public List<Responsavel> listarPorSetor(Long setorId) {
        return responsavelRepository.findBySetorIdOrderByNomeAsc(setorId);
    }

    public Optional<Responsavel> buscarPorId(Long id) {
        return responsavelRepository.findById(id);
    }

    @Transactional
    public Responsavel salvar(Responsavel responsavel) {
        return responsavelRepository.save(responsavel);
    }

    @Transactional
    public void excluir(Long id) {
        try {
            responsavelRepository.deleteById(id);
            responsavelRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("Não é possível excluir este servidor pois há termos de cautela vinculados a ele.");
        }
    }
}
