package br.com.multivix.projetobimestre2.repository;

import br.com.multivix.projetobimestre2.entity.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {
    List<Movimentacao> findAllByOrderByDataRetiradaDescIdDesc();
    List<Movimentacao> findByDataDevolucaoIsNullOrderByDataPrevistaDevolucaoAsc();
    List<Movimentacao> findByResponsavelSetorIdOrderByDataRetiradaDescIdDesc(Long setorId);
    List<Movimentacao> findByResponsavelSetorIdAndDataDevolucaoIsNullOrderByDataPrevistaDevolucaoAsc(Long setorId);
    long countByDataDevolucaoIsNull();
    long countByDataDevolucaoIsNullAndDataPrevistaDevolucaoBefore(LocalDate dataReferencia);
    boolean existsByEquipamentoIdAndDataDevolucaoIsNull(Long equipamentoId);
}
