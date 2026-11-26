package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.service.AuditoriaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public String listar(@RequestParam(value = "patrimonio", required = false) String patrimonio, Model model) {
        if (patrimonio != null && !patrimonio.isBlank()) {
            model.addAttribute("logs", auditoriaService.listarPorPatrimonio(patrimonio));
            model.addAttribute("patrimonioFiltro", patrimonio.trim().toUpperCase());
        } else {
            model.addAttribute("logs", auditoriaService.listarRecentes());
            model.addAttribute("patrimonioFiltro", "");
        }
        return "auditoria/lista";
    }
}