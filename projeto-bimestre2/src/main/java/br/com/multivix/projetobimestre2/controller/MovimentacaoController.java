package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.AnexoPatrimonial;
import br.com.multivix.projetobimestre2.entity.EstadoConservacao;
import br.com.multivix.projetobimestre2.entity.Movimentacao;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.AnexoService;
import br.com.multivix.projetobimestre2.service.EquipamentoService;
import br.com.multivix.projetobimestre2.service.MovimentacaoService;
import br.com.multivix.projetobimestre2.service.ResponsavelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;
    private final EquipamentoService equipamentoService;
    private final ResponsavelService responsavelService;
    private final AnexoService anexoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService,
                                  EquipamentoService equipamentoService,
                                  ResponsavelService responsavelService,
                                  AnexoService anexoService) {
        this.movimentacaoService = movimentacaoService;
        this.equipamentoService = equipamentoService;
        this.responsavelService = responsavelService;
        this.anexoService = anexoService;
    }

    @GetMapping
    public String listar(@RequestParam(defaultValue = "todas") String filtro, Model model) {
        List<Movimentacao> lista = "ativas".equalsIgnoreCase(filtro)
                ? movimentacaoService.listarAtivas()
                : movimentacaoService.listarTodas();

        Map<Long, List<AnexoPatrimonial>> anexosPorMovimentacao = new HashMap<>();
        for (Movimentacao m : lista) {
            anexosPorMovimentacao.put(m.getId(), anexoService.listarPorMovimentacao(m.getId()));
        }

        model.addAttribute("movimentacoes", lista);
        model.addAttribute("anexosPorMovimentacao", anexosPorMovimentacao);
        model.addAttribute("filtro", filtro);
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
        carregarCombos(model);
        return "movimentacoes/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("movimentacao") Movimentacao movimentacao,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        if (movimentacao.getEquipamento() == null || movimentacao.getEquipamento().getId() == null) {
            model.addAttribute("erro", "Selecione um equipamento disponível para retirada.");
            carregarCombos(model);
            return "movimentacoes/form";
        }
        if (movimentacao.getResponsavel() == null || movimentacao.getResponsavel().getId() == null) {
            model.addAttribute("erro", "Selecione o servidor responsável pela retirada.");
            carregarCombos(model);
            return "movimentacoes/form";
        }

        try {
            Movimentacao salva = movimentacaoService.registrarEmprestimo(movimentacao);
            redirectAttributes.addFlashAttribute("sucesso",
                    "Empréstimo registrado! Termo nº " + salva.getNumeroTermo() + " emitido com sucesso.");
            return "redirect:/movimentacoes/termo/" + salva.getId();
        } catch (RegraNegocioException e) {
            model.addAttribute("erro", e.getMessage());
            carregarCombos(model);
            return "movimentacoes/form";
        }
    }

    @PostMapping("/devolver/{id}")
    public String devolver(@PathVariable Long id,
                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataDevolucao,
                           @RequestParam(required = false) String observacaoDevolucao,
                           @RequestParam(required = false) EstadoConservacao estadoConservacao,
                           @RequestParam(defaultValue = "false") boolean avariaInformada,
                           RedirectAttributes redirectAttributes) {
        try {
            Movimentacao mov = movimentacaoService.registrarDevolucaoCompleta(
                    id, dataDevolucao, observacaoDevolucao, estadoConservacao, avariaInformada);
            String msg = mov.isDevolvidoComAvaria()
                    ? "Devolução com avaria registrada (" + mov.getNumeroTermo() + "). Equipamento encaminhado automaticamente para MANUTENÇÃO."
                    : "Devolução registrada com sucesso! Equipamento " + mov.getEquipamento().getPatrimonio() + " está disponível novamente.";
            redirectAttributes.addFlashAttribute("sucesso", msg);
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/movimentacoes";
    }

    @PostMapping("/prorrogar/{id}")
    public String prorrogar(@PathVariable Long id,
                            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate novaDataPrevista,
                            @RequestParam(required = false) String motivo,
                            RedirectAttributes redirectAttributes) {
        try {
            movimentacaoService.prorrogarPrazo(id, novaDataPrevista, motivo);
            redirectAttributes.addFlashAttribute("sucesso", "Prazo de devolução prorrogado com sucesso!");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/movimentacoes";
    }

    @GetMapping("/termo/{id}")
    public String termoResponsabilidade(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return movimentacaoService.buscarPorId(id)
                .map(mov -> {
                    model.addAttribute("movimentacao", mov);
                    model.addAttribute("anexos", anexoService.listarPorMovimentacao(mov.getId()));
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
            redirectAttributes.addFlashAttribute("sucesso", "Registro de movimentação encerrado excluído.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/movimentacoes";
    }

    private void carregarCombos(Model model) {
        model.addAttribute("equipamentosDisponiveis", equipamentoService.listarDisponiveis());
        model.addAttribute("responsaveis", responsavelService.listarTodos());
    }
}