package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.Movimentacao;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.EquipamentoService;
import br.com.multivix.projetobimestre2.service.MovimentacaoService;
import br.com.multivix.projetobimestre2.service.ResponsavelService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;
    private final EquipamentoService equipamentoService;
    private final ResponsavelService responsavelService;

    public MovimentacaoController(MovimentacaoService movimentacaoService,
                                  EquipamentoService equipamentoService,
                                  ResponsavelService responsavelService) {
        this.movimentacaoService = movimentacaoService;
        this.equipamentoService = equipamentoService;
        this.responsavelService = responsavelService;
    }

    private void carregarListasFormulario(Model model) {
        model.addAttribute("equipamentosDisponiveis", equipamentoService.listarDisponiveis());
        model.addAttribute("responsaveis", responsavelService.listarTodos());
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "todas") String filtro, Model model) {
        if ("ativas".equalsIgnoreCase(filtro)) {
            model.addAttribute("movimentacoes", movimentacaoService.listarAtivas());
            model.addAttribute("filtro", "ativas");
        } else {
            model.addAttribute("movimentacoes", movimentacaoService.listarTodas());
            model.addAttribute("filtro", "todas");
        }
        return "movimentacoes/lista";
    }

    @GetMapping("/nova")
    public String nova(@RequestParam(required = false) Long equipamentoId, Model model) {
        Movimentacao mov = new Movimentacao();
        mov.setDataRetirada(LocalDate.now());
        mov.setDataPrevistaDevolucao(LocalDate.now().plusDays(7));
        if (equipamentoId != null) {
            equipamentoService.buscarPorId(equipamentoId).ifPresent(mov::setEquipamento);
        }
        model.addAttribute("movimentacao", mov);
        carregarListasFormulario(model);
        return "movimentacoes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@Valid @ModelAttribute("movimentacao") Movimentacao movimentacao,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            carregarListasFormulario(model);
            return "movimentacoes/form";
        }
        try {
            Movimentacao salva = movimentacaoService.registrarEmprestimo(movimentacao);
            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Empréstimo registrado com sucesso! Termo gerado: " + salva.getNumeroTermo()
            );
            return "redirect:/movimentacoes";
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarListasFormulario(model);
            return "movimentacoes/form";
        }
    }

    @PostMapping("/devolver/{id}")
    public String devolver(@PathVariable Long id,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDevolucao,
                           @RequestParam(required = false) String observacaoDevolucao,
                           RedirectAttributes redirectAttributes) {
        try {
            Movimentacao devolvida = movimentacaoService.registrarDevolucao(id, dataDevolucao, observacaoDevolucao);
            redirectAttributes.addFlashAttribute(
                    "sucesso",
                    "Devolução registrada no termo " + devolvida.getNumeroTermo() + ". Equipamento disponível novamente."
            );
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/movimentacoes";
    }

    @GetMapping("/termo/{id}")
    public String termo(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return movimentacaoService.buscarPorId(id)
                .map(mov -> {
                    model.addAttribute("movimentacao", mov);
                    return "movimentacoes/termo";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("erro", "Termo de movimentação não encontrado.");
                    return "redirect:/movimentacoes";
                });
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            movimentacaoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Registro histórico excluído.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/movimentacoes";
    }
}
