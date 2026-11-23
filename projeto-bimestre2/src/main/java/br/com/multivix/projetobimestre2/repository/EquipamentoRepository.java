package br.com.multivix.projetobimestre2.repository;

import br.com.multivix.projetobimestre2.entity.Equipamento;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
    boolean existsByPatrimonioIgnoreCase(String patrimonio);
    boolean existsByPatrimonioIgnoreCaseAndIdNot(String patrimonio, Long id);
    Optional<Equipamento> findByPatrimonioIgnoreCase(String patrimonio);
    List<Equipamento> findBySituacaoOrderByNomeAsc(StatusEquipamento situacao);
    List<Equipamento> findAllByOrderByPatrimonioAsc();
    List<Equipamento> findByConferidoInventarioFalseOrderByPatrimonioAsc();
    long countBySituacao(StatusEquipamento situacao);
    long countByConferidoInventarioFalse();
}
