package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.PerfilAcesso;
import br.com.multivix.projetobimestre2.entity.Usuario;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.SetorService;
import br.com.multivix.projetobimestre2.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final SetorService setorService;

    public UsuarioController(UsuarioService usuarioService, SetorService setorService) {
        this.usuarioService = usuarioService;
        this.setorService = setorService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/primeiro-acesso")
    public String primeiroAcessoForm(@AuthenticationPrincipal Usuario logado, Model model) {
        if (logado == null) {
            return "redirect:/login";
        }
        if (!logado.isPrimeiroAcessoAtivo()) {
            return "redirect:/";
        }
        model.addAttribute("usuario", logado);
        return "primeiro-acesso";
    }

    @PostMapping("/primeiro-acesso")
    public String redefinirSenhaPrimeiroAcesso(@AuthenticationPrincipal Usuario logado,
                                               @RequestParam("novaSenha") String novaSenha,
                                               @RequestParam("confirmarSenha") String confirmarSenha,
                                               Model model,
                                               RedirectAttributes redirect) {
        if (logado == null) {
            return "redirect:/login";
        }
        try {
            Usuario atualizado = usuarioService.redefinirSenhaPrimeiroAcesso(logado.getId(), novaSenha, confirmarSenha);
            Authentication novaAuth = new UsernamePasswordAuthenticationToken(
                    atualizado, atualizado.getPassword(), atualizado.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(novaAuth);
            redirect.addFlashAttribute("sucesso", "Senha redefinida com sucesso! Bem-vindo(a) ao sistema.");
            return "redirect:/";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("usuario", logado);
            return "primeiro-acesso";
        }
    }

    @GetMapping("/usuarios")
    public String listar(Model model) {
        model.addAttribute("usuarios", usuarioService.listarTodos());
        return "usuarios/lista";
    }

    @GetMapping("/usuarios/novo")
    public String novo(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("perfis", PerfilAcesso.values());
        model.addAttribute("setores", setorService.listarTodos());
        return "usuarios/form";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("usuario", usuarioService.buscarPorId(id));
        model.addAttribute("perfis", PerfilAcesso.values());
        model.addAttribute("setores", setorService.listarTodos());
        return "usuarios/form";
    }

    @PostMapping("/usuarios/salvar")
    public String salvar(@Valid @ModelAttribute("usuario") Usuario usuario,
                         BindingResult result,
                         @RequestParam(value = "novaSenha", required = false) String novaSenha,
                         Model model,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("perfis", PerfilAcesso.values());
            model.addAttribute("setores", setorService.listarTodos());
            return "usuarios/form";
        }
        try {
            usuarioService.salvar(usuario, novaSenha);
            redirect.addFlashAttribute("sucesso", "Usuário e permissões salvos com sucesso!");
            return "redirect:/usuarios";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("perfis", PerfilAcesso.values());
            model.addAttribute("setores", setorService.listarTodos());
            return "usuarios/form";
        }
    }

    @PostMapping("/usuarios/status/{id}")
    public String alternarStatus(@PathVariable Long id,
                                 @AuthenticationPrincipal Usuario logado,
                                 RedirectAttributes redirect) {
        try {
            usuarioService.alternarStatus(id, logado != null ? logado.getEmail() : "");
            redirect.addFlashAttribute("sucesso", "Status de acesso do usuário atualizado.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    @PostMapping("/usuarios/excluir/{id}")
    public String excluir(@PathVariable Long id,
                          @AuthenticationPrincipal Usuario logado,
                          RedirectAttributes redirect) {
        try {
            usuarioService.excluir(id, logado != null ? logado.getEmail() : "");
            redirect.addFlashAttribute("sucesso", "Usuário excluído com sucesso.");
        } catch (RegraNegocioException e) {
            redirect.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/usuarios";
    }
}
