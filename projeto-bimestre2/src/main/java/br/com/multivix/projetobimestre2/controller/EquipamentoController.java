package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.Equipamento;
import br.com.multivix.projetobimestre2.entity.EstadoConservacao;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.CategoriaService;
import br.com.multivix.projetobimestre2.service.EquipamentoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/equipamentos")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;
    private final CategoriaService categoriaService;

    public EquipamentoController(EquipamentoService equipamentoService, CategoriaService categoriaService) {
        this.equipamentoService = equipamentoService;
        this.categoriaService = categoriaService;
    }

    private void carregarListasFormulario(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("estadosConservacao", EstadoConservacao.values());
        model.addAttribute("situacoes", StatusEquipamento.values());
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusEquipamento situacao, Model model) {
        if (situacao != null) {
            model.addAttribute("equipamentos", equipamentoService.listarPorSituacao(situacao));
            model.addAttribute("situacaoFiltro", situacao);
        } else {
            model.addAttribute("equipamentos", equipamentoService.listarTodos());
        }
        model.addAttribute("situacoes", StatusEquipamento.values());
        return "equipamentos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        Equipamento equipamento = new Equipamento();
        equipamento.setDataAquisicao(LocalDate.now());
        equipamento.setEstadoConservacao(EstadoConservacao.BOM);
        equipamento.setSituacao(StatusEquipamento.DISPONIVEL);
        model.addAttribute("equipamento", equipamento);
        carregarListasFormulario(model);
        return "equipamentos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return equipamentoService.buscarPorId(id)
                .map(equipamento -> {
                    model.addAttribute("equipamento", equipamento);
                    carregarListasFormulario(model);
                    return "equipamentos/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Equipamento não encontrado.");
                    return "redirect:/equipamentos";
                });
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("equipamento") Equipamento equipamento,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            carregarListasFormulario(model);
            return "equipamentos/form";
        }
        try {
            equipamentoService.salvar(equipamento);
            redirectAttributes.addFlashAttribute("sucesso", "Patrimônio salvo com sucesso!");
            return "redirect:/equipamentos";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarListasFormulario(model);
            return "equipamentos/form";
        }
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipamentoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Patrimônio excluído com sucesso.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/equipamentos";
    }
}
