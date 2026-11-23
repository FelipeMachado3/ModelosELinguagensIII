package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.Responsavel;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.ResponsavelService;
import br.com.multivix.projetobimestre2.service.SetorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/responsaveis")
public class ResponsavelController {

    private final ResponsavelService responsavelService;
    private final SetorService setorService;

    public ResponsavelController(ResponsavelService responsavelService, SetorService setorService) {
        this.responsavelService = responsavelService;
        this.setorService = setorService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) Long setorId, Model model) {
        if (setorId != null) {
            model.addAttribute("responsaveis", responsavelService.listarPorSetor(setorId));
            model.addAttribute("setorFiltro", setorId);
        } else {
            model.addAttribute("responsaveis", responsavelService.listarTodos());
        }
        model.addAttribute("setores", setorService.listarTodos());
        return "responsaveis/lista";
    }

    @GetMapping("/novo")
    public String novo(@RequestParam(required = false) Long setorId, Model model) {
        Responsavel resp = new Responsavel();
        if (setorId != null) {
            setorService.buscarPorId(setorId).ifPresent(resp::setSetor);
        }
        model.addAttribute("responsavel", resp);
        model.addAttribute("setores", setorService.listarTodos());
        return "responsaveis/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return responsavelService.buscarPorId(id)
                .map(responsavel -> {
                    model.addAttribute("responsavel", responsavel);
                    model.addAttribute("setores", setorService.listarTodos());
                    return "responsaveis/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Servidor responsável não encontrado.");
                    return "redirect:/responsaveis";
                });
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("responsavel") Responsavel responsavel,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("setores", setorService.listarTodos());
            return "responsaveis/form";
        }
        responsavelService.salvar(responsavel);
        redirectAttributes.addFlashAttribute("sucesso", "Servidor responsável cadastrado com sucesso!");
        return "redirect:/responsaveis";
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            responsavelService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Servidor responsável excluído com sucesso.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/responsaveis";
    }
}
