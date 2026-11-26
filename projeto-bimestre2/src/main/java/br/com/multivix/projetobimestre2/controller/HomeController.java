package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.EquipamentoService;
import br.com.multivix.projetobimestre2.service.MovimentacaoService;
import br.com.multivix.projetobimestre2.service.SetorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

@Controller
public class HomeController {

    private final EquipamentoService equipamentoService;
    private final MovimentacaoService movimentacaoService;
    private final SetorService setorService;

    public HomeController(EquipamentoService equipamentoService,
                          MovimentacaoService movimentacaoService,
                          SetorService setorService) {
        this.equipamentoService = equipamentoService;
        this.movimentacaoService = movimentacaoService;
        this.setorService = setorService;
    }

    @GetMapping("/")
    public String index(Model model) {
        long total = equipamentoService.contarTotal();
        long disponiveis = equipamentoService.contarPorSituacao(StatusEquipamento.DISPONIVEL);
        long emprestados = equipamentoService.contarPorSituacao(StatusEquipamento.EMPRESTADO);
        long atrasadas = movimentacaoService.contarAtrasadas();

        String pctEmUso = total > 0
                ? String.format(Locale.forLanguageTag("pt-BR"), "%.1f%%", (emprestados * 100.0) / total)
                : "0,0%";
        String pctDisponivel = total > 0
                ? String.format(Locale.forLanguageTag("pt-BR"), "%.1f%%", (disponiveis * 100.0) / total)
                : "0,0%";

        model.addAttribute("totalEquipamentos", total);
        model.addAttribute("totalDisponiveis", disponiveis);
        model.addAttribute("totalEmprestados", emprestados);
        model.addAttribute("totalAtrasadas", atrasadas);
        model.addAttribute("percentualEmUso", pctEmUso);
        model.addAttribute("percentualDisponivel", pctDisponivel);
        model.addAttribute("totalSetores", setorService.listarTodos().size());
        model.addAttribute("movimentacoesRecentes", movimentacaoService.listarTodas());
        model.addAttribute("proximasDevolucoes", movimentacaoService.listarAtivas());
        model.addAttribute("pendentesInventario", equipamentoService.listarPendentesInventario());
        return "index";
    }

    @PostMapping("/inventario/conferir/{id}")
    public String conferirItem(@PathVariable Long id,
                               @RequestParam(value = "statusDivergencia", required = false) String statusDivergencia,
                               @RequestParam(value = "observacao", required = false) String observacao,
                               RedirectAttributes redirectAttributes) {
        try {
            equipamentoService.marcarComoConferidoComDivergencia(id, statusDivergencia, observacao);
            redirectAttributes.addFlashAttribute("sucesso", "Bem patrimonial conferido no ciclo de inventário!");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/inventario/reiniciar")
    public String reiniciarInventario(RedirectAttributes redirectAttributes) {
        equipamentoService.reiniciarCicloInventario();
        redirectAttributes.addFlashAttribute("sucesso", "Novo ciclo de inventário iniciado! Todos os bens foram marcados para conferência.");
        return "redirect:/configuracoes";
    }
}