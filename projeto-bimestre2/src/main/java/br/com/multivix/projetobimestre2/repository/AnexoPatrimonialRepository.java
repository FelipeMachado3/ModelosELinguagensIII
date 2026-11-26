package br.com.multivix.projetobimestre2.repository;

import br.com.multivix.projetobimestre2.entity.AnexoPatrimonial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnexoPatrimonialRepository extends JpaRepository<AnexoPatrimonial, Long> {
    List<AnexoPatrimonial> findByEquipamentoIdOrderByDataUploadDesc(Long equipamentoId);
    List<AnexoPatrimonial> findByMovimentacaoIdOrderByDataUploadDesc(Long movimentacaoId);
    long countByEquipamentoId(Long equipamentoId);
    long countByMovimentacaoId(Long movimentacaoId);
    boolean existsByEquipamentoIdAndTipoDocumentoIgnoreCase(Long equipamentoId, String tipoDocumento);
    boolean existsByMovimentacaoIdAndTipoDocumentoIgnoreCase(Long movimentacaoId, String tipoDocumento);
}