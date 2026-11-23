package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Setor;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.SetorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SetorService {

    private final SetorRepository setorRepository;

    public SetorService(SetorRepository setorRepository) {
        this.setorRepository = setorRepository;
    }

    public List<Setor> listarTodos() {
        return setorRepository.findAllByOrderByNomeAsc();
    }

    public Optional<Setor> buscarPorId(Long id) {
        return setorRepository.findById(id);
    }

    @Transactional
    public Setor salvar(Setor setor) {
        String siglaNorm = setor.getSigla() != null ? setor.getSigla().trim().toUpperCase() : "";
        setor.setSigla(siglaNorm);

        boolean siglaDuplicada = (setor.getId() == null)
                ? setorRepository.existsBySiglaIgnoreCase(siglaNorm)
                : setorRepository.existsBySiglaIgnoreCaseAndIdNot(siglaNorm, setor.getId());

        if (siglaDuplicada) {
            throw new RegraNegocioException("Já existe um setor cadastrado com a sigla '" + siglaNorm + "'.");
        }

        return setorRepository.save(setor);
    }

    @Transactional
    public void excluir(Long id) {
        try {
            setorRepository.deleteById(id);
            setorRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("Não é possível excluir este setor pois existem servidores responsáveis vinculados a ele.");
        }
    }
}
