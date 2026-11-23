package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.Movimentacao;
import br.com.multivix.projetobimestre2.entity.Setor;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/relatorios")
public class RelatorioController {

    private final EquipamentoService equipamentoService;
    private final MovimentacaoService movimentacaoService;
    private final SetorService setorService;
    private final ResponsavelService responsavelService;

    public RelatorioController(EquipamentoService equipamentoService,
                               MovimentacaoService movimentacaoService,
                               SetorService setorService,
                               ResponsavelService responsavelService) {
        this.equipamentoService = equipamentoService;
        this.movimentacaoService = movimentacaoService;
        this.setorService = setorService;
        this.responsavelService = responsavelService;
    }

    @GetMapping
    public String relatorios(@RequestParam(required = false) Long setorId, Model model) {
        List<Setor> setores = setorService.listarTodos();
        model.addAttribute("setores", setores);
        model.addAttribute("setorIdSelecionado", setorId);
        model.addAttribute("dataEmissao", LocalDate.now());

        if (setorId != null) {
            Setor setorSelecionado = setorService.buscarPorId(setorId).orElse(null);
            List<Movimentacao> movsSetor = movimentacaoService.listarPorSetor(setorId);
            long ativasSetor = movsSetor.stream().filter(Movimentacao::isAtiva).count();
            long atrasadasSetor = movsSetor.stream().filter(Movimentacao::isAtrasada).count();

            model.addAttribute("setorSelecionado", setorSelecionado);
            model.addAttribute("movimentacoesRelatorio", movsSetor);
            model.addAttribute("responsaveisSetor", responsavelService.listarPorSetor(setorId));
            model.addAttribute("ativasSetor", ativasSetor);
            model.addAttribute("atrasadasSetor", atrasadasSetor);
        } else {
            model.addAttribute("movimentacoesRelatorio", movimentacaoService.listarTodas());
        }

        model.addAttribute("totalEquipamentos", equipamentoService.contarTotal());
        model.addAttribute("totalDisponiveis", equipamentoService.contarPorSituacao(StatusEquipamento.DISPONIVEL));
        model.addAttribute("totalEmprestados", equipamentoService.contarPorSituacao(StatusEquipamento.EMPRESTADO));
        model.addAttribute("totalManutencao", equipamentoService.contarPorSituacao(StatusEquipamento.MANUTENCAO));
        model.addAttribute("totalAtrasadas", movimentacaoService.contarAtrasadas());
        model.addAttribute("equipamentos", equipamentoService.listarTodos());
        return "relatorios/index";
    }
}
