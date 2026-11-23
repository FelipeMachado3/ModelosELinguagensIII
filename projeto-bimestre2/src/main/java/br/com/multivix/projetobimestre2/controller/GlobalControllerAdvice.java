package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.Movimentacao;
import br.com.multivix.projetobimestre2.entity.StatusEquipamento;
import br.com.multivix.projetobimestre2.service.EquipamentoService;
import br.com.multivix.projetobimestre2.service.MovimentacaoService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalControllerAdvice {

    private final EquipamentoService equipamentoService;
    private final MovimentacaoService movimentacaoService;

    public GlobalControllerAdvice(EquipamentoService equipamentoService,
                                  MovimentacaoService movimentacaoService) {
        this.equipamentoService = equipamentoService;
        this.movimentacaoService = movimentacaoService;
    }

    @ModelAttribute("badgeEmprestimosAtivos")
    public long badgeEmprestimosAtivos() {
        return equipamentoService.contarPorSituacao(StatusEquipamento.EMPRESTADO);
    }

    @ModelAttribute("badgeAtrasadas")
    public long badgeAtrasadas() {
        return movimentacaoService.contarAtrasadas();
    }

    @ModelAttribute("badgePendentesInventario")
    public long badgePendentesInventario() {
        return equipamentoService.contarPendentesInventario();
    }

    @ModelAttribute("notificacoesAtrasadas")
    public List<Movimentacao> notificacoesAtrasadas() {
        return movimentacaoService.listarAtivas().stream()
                .filter(Movimentacao::isAtrasada)
                .limit(5)
                .toList();
    }
}
