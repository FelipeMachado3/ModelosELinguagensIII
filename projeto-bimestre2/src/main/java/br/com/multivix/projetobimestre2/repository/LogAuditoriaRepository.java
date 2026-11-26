package br.com.multivix.projetobimestre2.repository;

import br.com.multivix.projetobimestre2.entity.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findTop200ByOrderByDataHoraDesc();
    List<LogAuditoria> findByPatrimonioRefOrderByDataHoraDesc(String patrimonioRef);
    List<LogAuditoria> findByUsuarioEmailIgnoreCaseOrderByDataHoraDesc(String usuarioEmail);
}