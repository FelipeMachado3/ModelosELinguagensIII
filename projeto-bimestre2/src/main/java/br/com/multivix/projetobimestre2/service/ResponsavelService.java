package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Responsavel;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.MovimentacaoRepository;
import br.com.multivix.projetobimestre2.repository.ResponsavelRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ResponsavelService {

    private final ResponsavelRepository responsavelRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AuditoriaService auditoriaService;

    public ResponsavelService(ResponsavelRepository responsavelRepository,
                              MovimentacaoRepository movimentacaoRepository,
                              AuditoriaService auditoriaService) {
        this.responsavelRepository = responsavelRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.auditoriaService = auditoriaService;
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
        Responsavel salvo = responsavelRepository.save(responsavel);
        auditoriaService.registrar(
                "RESPONSAVEIS",
                "SALVAR_RESPONSAVEL",
                "Responsavel",
                salvo.getId(),
                null,
                "Responsável '" + salvo.getNome() + "' (Mat. " + salvo.getMatricula() + ") salvo."
        );
        return salvo;
    }

    @Transactional
    public void excluir(Long id) {
        Responsavel responsavel = responsavelRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Servidor responsável não encontrado."));

        if (movimentacaoRepository.existsByResponsavelIdAndDataDevolucaoIsNull(id)) {
            throw new RegraNegocioException("Não é possível excluir este servidor pois existem cautelas em aberto sob sua responsabilidade.");
        }

        try {
            String nome = responsavel.getNome();
            responsavelRepository.deleteById(id);
            responsavelRepository.flush();
            auditoriaService.registrar("RESPONSAVEIS", "EXCLUIR_RESPONSAVEL", "Responsavel", id, null, "Responsável '" + nome + "' excluído.");
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("Não é possível excluir este servidor pois há termos de cautela vinculados a ele.");
        }
    }
}