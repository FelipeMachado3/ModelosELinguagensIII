package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.Usuario;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          AuditoriaService auditoriaService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));
    }

    @Transactional
    public Usuario salvar(Usuario usuario, String novaSenha) {
        boolean novoUsuario = (usuario.getId() == null);
        if (novoUsuario) {
            if (usuarioRepository.existsByEmailIgnoreCase(usuario.getEmail())) {
                throw new RegraNegocioException("Já existe um usuário cadastrado com este e-mail.");
            }
            if (novaSenha == null || novaSenha.isBlank()) {
                throw new RegraNegocioException("Defina uma senha inicial para o novo usuário.");
            }
            usuario.setSenha(passwordEncoder.encode(novaSenha));
            usuario.setPrimeiroAcesso(true);
            usuario.setAtivo(true);
        } else {
            Usuario existente = buscarPorId(usuario.getId());
            if (!existente.getEmail().equalsIgnoreCase(usuario.getEmail())
                    && usuarioRepository.existsByEmailIgnoreCase(usuario.getEmail())) {
                throw new RegraNegocioException("Este e-mail já está em uso por outro usuário.");
            }
            if (novaSenha != null && !novaSenha.isBlank()) {
                usuario.setSenha(passwordEncoder.encode(novaSenha));
                usuario.setPrimeiroAcesso(true);
            } else {
                usuario.setSenha(existente.getSenha());
                usuario.setPrimeiroAcesso(existente.getPrimeiroAcesso());
            }
            usuario.setUltimoLogin(existente.getUltimoLogin());
        }

        Usuario salvo = usuarioRepository.save(usuario);
        auditoriaService.registrar(
                "USUARIOS",
                novoUsuario ? "CRIAR_USUARIO" : "ATUALIZAR_USUARIO",
                "Usuario",
                salvo.getId(),
                null,
                "Usuário '" + salvo.getEmail() + "' (" + salvo.getPerfil() + ") " + (novoUsuario ? "cadastrado" : "atualizado") + "."
        );
        return salvo;
    }

    @Transactional
    public void registrarUltimoLogin(Long id) {
        usuarioRepository.findById(id).ifPresent(u -> {
            u.setUltimoLogin(LocalDateTime.now());
            usuarioRepository.save(u);
        });
    }

    @Transactional
    public Usuario redefinirSenhaPrimeiroAcesso(Long id, String novaSenha, String confirmarSenha) {
        if (novaSenha == null || novaSenha.trim().length() < 6) {
            throw new RegraNegocioException("A nova senha deve ter no mínimo 6 caracteres.");
        }
        if (!novaSenha.equals(confirmarSenha)) {
            throw new RegraNegocioException("A confirmação da senha não confere.");
        }
        Usuario usuario = buscarPorId(id);
        if (passwordEncoder.matches(novaSenha, usuario.getSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da senha temporária atual.");
        }
        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuario.setPrimeiroAcesso(false);
        Usuario atualizado = usuarioRepository.save(usuario);

        auditoriaService.registrar(
                "USUARIOS",
                "REDEFINIR_SENHA_PRIMEIRO_ACESSO",
                "Usuario",
                atualizado.getId(),
                null,
                "Usuário '" + atualizado.getEmail() + "' concluiu a troca de senha do primeiro acesso."
        );
        return atualizado;
    }

    @Transactional
    public void alternarStatus(Long id, String emailLogado) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getEmail().equalsIgnoreCase(emailLogado) || "manager@patrimonio.local".equalsIgnoreCase(usuario.getEmail())) {
            throw new RegraNegocioException("Você não pode bloquear o seu próprio usuário ou a conta Manager principal.");
        }
        usuario.setAtivo(!usuario.isAtivo());
        usuarioRepository.save(usuario);

        auditoriaService.registrar(
                "USUARIOS",
                usuario.isAtivo() ? "ATIVAR_USUARIO" : "INATIVAR_USUARIO",
                "Usuario",
                usuario.getId(),
                null,
                "Status do usuário '" + usuario.getEmail() + "' alterado para " + (usuario.isAtivo() ? "ATIVO" : "INATIVO") + "."
        );
    }

    @Transactional
    public void excluir(Long id, String emailLogado) {
        Usuario usuario = buscarPorId(id);
        if (usuario.getEmail().equalsIgnoreCase(emailLogado) || "manager@patrimonio.local".equalsIgnoreCase(usuario.getEmail())) {
            throw new RegraNegocioException("Você não pode excluir o seu próprio usuário ou a conta Manager principal.");
        }
        if (auditoriaService.usuarioPossuiHistorico(usuario.getEmail())) {
            throw new RegraNegocioException(
                    "Este usuário já possui histórico de operações no sistema e não pode ser excluído fisicamente. Utilize a inativação."
            );
        }
        String emailRemovido = usuario.getEmail();
        usuarioRepository.delete(usuario);

        auditoriaService.registrar(
                "USUARIOS",
                "EXCLUIR_USUARIO",
                "Usuario",
                id,
                null,
                "Usuário '" + emailRemovido + "' excluído permanentemente (sem histórico prévio)."
        );
    }
}