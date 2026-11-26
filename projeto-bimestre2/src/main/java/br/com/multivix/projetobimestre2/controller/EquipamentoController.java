package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.Equipamento;
import br.com.multivix.projetobimestre2.entity.EstadoConservacao;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.AnexoService;
import br.com.multivix.projetobimestre2.service.CategoriaService;
import br.com.multivix.projetobimestre2.service.EquipamentoService;
import br.com.multivix.projetobimestre2.service.SetorService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/equipamentos")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;
    private final CategoriaService categoriaService;
    private final SetorService setorService;
    private final AnexoService anexoService;

    public EquipamentoController(EquipamentoService equipamentoService,
                                 CategoriaService categoriaService,
                                 SetorService setorService,
                                 AnexoService anexoService) {
        this.equipamentoService = equipamentoService;
        this.categoriaService = categoriaService;
        this.setorService = setorService;
        this.anexoService = anexoService;
    }

    @GetMapping
    public String listar(@RequestParam(required = false) StatusEquipamento situacao, Model model) {
        model.addAttribute("equipamentos", equipamentoService.listarPorSituacao(situacao));
        model.addAttribute("situacaoFiltro", situacao);
        model.addAttribute("situacoes", StatusEquipamento.values());
        return "equipamentos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("equipamento", new Equipamento());
        carregarCombos(model, null);
        return "equipamentos/form";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return equipamentoService.buscarPorId(id)
                .map(eq -> {
                    model.addAttribute("equipamento", eq);
                    carregarCombos(model, eq.getId());
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
            carregarCombos(model, equipamento.getId());
            return "equipamentos/form";
        }
        try {
            Equipamento salvo = equipamentoService.salvar(equipamento);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Equipamento tombamento #" + salvo.getPatrimonio() + " salvo com sucesso!");
            return "redirect:/equipamentos";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarCombos(model, equipamento.getId());
            return "equipamentos/form";
        }
    }

    @PostMapping("/conferir/{id}")
    public String conferirInventario(@PathVariable Long id,
                                     @RequestParam(value = "statusDivergencia", required = false) String statusDivergencia,
                                     @RequestParam(value = "observacao", required = false) String observacao,
                                     RedirectAttributes redirectAttributes) {
        try {
            equipamentoService.marcarComoConferidoComDivergencia(id, statusDivergencia, observacao);
            redirectAttributes.addFlashAttribute("sucesso", "Conferência patrimonial registrada no inventário!");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/equipamentos";
    }

    @PostMapping("/baixar/{id}")
    public String baixarPatrimonio(@PathVariable Long id,
                                   @RequestParam("motivoBaixa") String motivoBaixa,
                                   RedirectAttributes redirectAttributes) {
        try {
            equipamentoService.baixarPatrimonio(id, motivoBaixa);
            redirectAttributes.addFlashAttribute("sucesso", "Baixa patrimonial efetivada com sucesso.");
            return "redirect:/equipamentos";
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
            return "redirect:/equipamentos/editar/" + id;
        }
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            equipamentoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Equipamento excluído do cadastro.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/equipamentos";
    }

    private void carregarCombos(Model model, Long equipamentoId) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("setores", setorService.listarTodos());
        model.addAttribute("estadosConservacao", EstadoConservacao.values());
        model.addAttribute("situacoes", StatusEquipamento.values());
        model.addAttribute("anexos", equipamentoId != null ? anexoService.listarPorEquipamento(equipamentoId) : List.of());
    }
}