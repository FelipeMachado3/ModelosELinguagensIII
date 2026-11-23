package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.service.CategoriaService;
import br.com.multivix.projetobimestre2.service.EquipamentoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/configuracoes")
public class ConfiguracaoController {

    private final CategoriaService categoriaService;
    private final EquipamentoService equipamentoService;

    public ConfiguracaoController(CategoriaService categoriaService, EquipamentoService equipamentoService) {
        this.categoriaService = categoriaService;
        this.equipamentoService = equipamentoService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("categorias", categoriaService.listarTodas());
        model.addAttribute("totalEquipamentos", equipamentoService.contarTotal());
        model.addAttribute("pendentesInventario", equipamentoService.contarPendentesInventario());
        return "configuracoes/index";
    }
}
