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
        return "setores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("setor", new Setor());
        model.addAttribute("setores", setorService.listarTodos());
        model.addAttribute("responsaveis", responsavelService.listarTodos());
        return "setores/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return setorService.buscarPorId(id)
                .map(setor -> {
                    model.addAttribute("setor", setor);
                    model.addAttribute("setores", setorService.listarTodos());
                    model.addAttribute("responsaveis", responsavelService.listarTodos());
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
            model.addAttribute("setores", setorService.listarTodos());
            model.addAttribute("responsaveis", responsavelService.listarTodos());
            return "setores/form";
        }
        try {
            setorService.salvar(setor);
            redirectAttributes.addFlashAttribute("sucesso", "Unidade administrativa salva com sucesso!");
            return "redirect:/setores";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            model.addAttribute("setores", setorService.listarTodos());
            model.addAttribute("responsaveis", responsavelService.listarTodos());
            return "setores/form";
        }
    }

    @PostMapping("/{id}/inventario/abrir")
    public String abrirInventario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            setorService.abrirInventarioSetor(id);
            redirectAttributes.addFlashAttribute("sucesso", "Inventário do setor aberto. Transferências definitivas congeladas.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/setores";
    }

    @PostMapping("/{id}/inventario/encerrar")
    public String encerrarInventario(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            setorService.encerrarInventarioSetor(id);
            redirectAttributes.addFlashAttribute("sucesso", "Inventário do setor encerrado.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/setores";
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