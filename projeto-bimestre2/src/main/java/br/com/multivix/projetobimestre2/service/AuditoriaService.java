package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.LogAuditoria;
import br.com.multivix.projetobimestre2.entity.Usuario;
import br.com.multivix.projetobimestre2.repository.LogAuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;

    public AuditoriaService(LogAuditoriaRepository logAuditoriaRepository) {
        this.logAuditoriaRepository = logAuditoriaRepository;
    }

    @Transactional
    public void registrar(String modulo, String acao, String detalhes) {
        registrar(modulo, acao, null, null, null, detalhes);
    }

    @Transactional
    public void registrar(String modulo,
                          String acao,
                          String entidadeTipo,
                          Long entidadeId,
                          String patrimonioRef,
                          String detalhes) {
        LogAuditoria log = new LogAuditoria();
        log.setDataHora(LocalDateTime.now());
        log.setModulo(modulo);
        log.setAcao(acao);
        log.setEntidadeTipo(entidadeTipo);
        log.setEntidadeId(entidadeId);
        log.setPatrimonioRef(patrimonioRef);
        log.setDetalhes(truncar(detalhes, 1000));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Usuario usuario) {
            log.setUsuarioEmail(usuario.getEmail());
            log.setUsuarioNome(usuario.getNome());
            log.setPerfilUsuario(usuario.getPerfil() != null ? usuario.getPerfil().name() : "DESCONHECIDO");
        } else if (auth != null && auth.isAuthenticated()) {
            log.setUsuarioEmail(auth.getName());
            log.setUsuarioNome(auth.getName());
            log.setPerfilUsuario("AUTENTICADO");
        } else {
            log.setUsuarioEmail("sistema@patrimonio.local");
            log.setUsuarioNome("Sistema");
            log.setPerfilUsuario("SISTEMA");
        }

        logAuditoriaRepository.save(log);
    }

    public List<LogAuditoria> listarRecentes() {
        return logAuditoriaRepository.findTop200ByOrderByDataHoraDesc();
    }

    public List<LogAuditoria> listarPorPatrimonio(String patrimonio) {
        if (patrimonio == null || patrimonio.isBlank()) {
            return List.of();
        }
        return logAuditoriaRepository.findByPatrimonioRefOrderByDataHoraDesc(patrimonio.trim().toUpperCase());
    }

    public boolean usuarioPossuiHistorico(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return !logAuditoriaRepository.findByUsuarioEmailIgnoreCaseOrderByDataHoraDesc(email.trim()).isEmpty();
    }

    private String truncar(String texto, int max) {
        if (texto == null) return null;
        return texto.length() > max ? texto.substring(0, max) : texto;
    }
}