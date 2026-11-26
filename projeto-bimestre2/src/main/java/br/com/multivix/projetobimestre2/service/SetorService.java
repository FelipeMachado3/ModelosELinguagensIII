package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Setor;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.EquipamentoRepository;
import br.com.multivix.projetobimestre2.repository.ResponsavelRepository;
import br.com.multivix.projetobimestre2.repository.SetorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SetorService {

    private final SetorRepository setorRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final ResponsavelRepository responsavelRepository;
    private final AuditoriaService auditoriaService;

    public SetorService(SetorRepository setorRepository,
                        EquipamentoRepository equipamentoRepository,
                        ResponsavelRepository responsavelRepository,
                        AuditoriaService auditoriaService) {
        this.setorRepository = setorRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.responsavelRepository = responsavelRepository;
        this.auditoriaService = auditoriaService;
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

        boolean transferenciaResponsavel = false;
        if (setor.getId() != null) {
            Setor existente = setorRepository.findById(setor.getId())
                    .orElseThrow(() -> new RegraNegocioException("Setor não encontrado."));
            setor.setInventarioAberto(existente.isInventarioAberto());
            if (!Objects.equals(existente.getResponsavelTitularId(), setor.getResponsavelTitularId())) {
                setor.setDataTransferenciaResponsavel(LocalDate.now());
                transferenciaResponsavel = true;
            } else {
                setor.setDataTransferenciaResponsavel(existente.getDataTransferenciaResponsavel());
            }
        } else if (setor.getResponsavelTitularId() != null && setor.getDataTransferenciaResponsavel() == null) {
            setor.setDataTransferenciaResponsavel(LocalDate.now());
        }

        Setor salvo = setorRepository.save(setor);
        auditoriaService.registrar(
                "SETORES",
                transferenciaResponsavel ? "TRANSFERIR_RESPONSAVEL_SETOR" : "SALVAR_SETOR",
                "Setor",
                salvo.getId(),
                null,
                "Setor '" + salvo.getIdentificacao() + "' salvo"
                        + (transferenciaResponsavel ? " com transferência de responsável titular em " + LocalDate.now() : "") + "."
        );
        return salvo;
    }

    @Transactional
    public void abrirInventarioSetor(Long id) {
        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Setor não encontrado."));
        setor.setInventarioAberto(true);
        setorRepository.save(setor);
        auditoriaService.registrar(
                "INVENTARIO",
                "ABRIR_INVENTARIO_SETOR",
                "Setor",
                setor.getId(),
                null,
                "Ciclo de inventário aberto para o setor '" + setor.getIdentificacao() + "'. Transferências definitivas congeladas."
        );
    }

    @Transactional
    public void encerrarInventarioSetor(Long id) {
        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Setor não encontrado."));
        setor.setInventarioAberto(false);
        setorRepository.save(setor);
        auditoriaService.registrar(
                "INVENTARIO",
                "ENCERRAR_INVENTARIO_SETOR",
                "Setor",
                setor.getId(),
                null,
                "Ciclo de inventário encerrado para o setor '" + setor.getIdentificacao() + "'."
        );
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = setorRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Setor não encontrado."));

        if (setor.isInventarioAberto()) {
            throw new RegraNegocioException("Não é possível excluir um setor com conferência de inventário aberta.");
        }
        if (equipamentoRepository.existsBySetorIdAndSituacaoNot(id, StatusEquipamento.BAIXADO)) {
            throw new RegraNegocioException("Não é possível excluir este setor pois existem bens patrimoniais ativos vinculados a ele.");
        }
        if (responsavelRepository.existsBySetorIdAndAtivoTrue(id)) {
            throw new RegraNegocioException("Não é possível excluir este setor pois existem responsáveis ativos vinculados a ele.");
        }

        try {
            String ident = setor.getIdentificacao();
            setorRepository.deleteById(id);
            setorRepository.flush();
            auditoriaService.registrar("SETORES", "EXCLUIR_SETOR", "Setor", id, null, "Setor '" + ident + "' excluído.");
        } catch (DataIntegrityViolationException e) {
            throw new RegraNegocioException("Não é possível excluir este setor pois existem registros vinculados a ele.");
        }
    }
}