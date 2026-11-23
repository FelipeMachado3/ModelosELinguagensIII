package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.Setor;
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
@RequestMapping("/setores")
public class SetorController {

    private final SetorService setorService;
    private final ResponsavelService responsavelService;

    public SetorController(SetorService setorService, ResponsavelService responsavelService) {
        this.setorService = setorService;
        this.responsavelService = responsavelService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("setores", setorService.listarTodos());
        model.addAttribute("responsaveis", responsavelService.listarTodos());
        return "setores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("setor", new Setor());
        return "setores/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return setorService.buscarPorId(id)
                .map(setor -> {
                    model.addAttribute("setor", setor);
                    return "setores/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Setor não encontrado.");
                    return "redirect:/setores";
                });
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("setor") Setor setor,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "setores/form";
        }
        try {
            setorService.salvar(setor);
            redirectAttributes.addFlashAttribute("sucesso", "Setor administrativo salvo com sucesso!");
            return "redirect:/setores";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            return "setores/form";
        }
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            setorService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Setor excluído com sucesso.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/setores";
    }
}
