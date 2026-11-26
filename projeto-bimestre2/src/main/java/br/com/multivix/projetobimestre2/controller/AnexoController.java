package br.com.multivix.projetobimestre2.controller;

import br.com.multivix.projetobimestre2.entity.AnexoPatrimonial;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.service.AnexoService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/anexos")
public class AnexoController {

    private final AnexoService anexoService;

    public AnexoController(AnexoService anexoService) {
        this.anexoService = anexoService;
    }

    @PostMapping("/equipamento/{equipamentoId}")
    public String uploadEquipamento(@PathVariable Long equipamentoId,
                                    @RequestParam("arquivo") MultipartFile arquivo,
                                    @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
                                    @RequestParam(value = "descricao", required = false) String descricao,
                                    RedirectAttributes redirectAttributes) {
        try {
            anexoService.anexarAoEquipamento(equipamentoId, arquivo, tipoDocumento, descricao);
            redirectAttributes.addFlashAttribute("sucesso", "Anexo vinculado ao patrimônio com sucesso!");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/equipamentos/editar/" + equipamentoId;
    }

    @PostMapping("/movimentacao/{movimentacaoId}")
    public String uploadMovimentacao(@PathVariable Long movimentacaoId,
                                     @RequestParam("arquivo") MultipartFile arquivo,
                                     @RequestParam(value = "tipoDocumento", required = false) String tipoDocumento,
                                     @RequestParam(value = "descricao", required = false) String descricao,
                                     RedirectAttributes redirectAttributes) {
        try {
            anexoService.anexarAMovimentacao(movimentacaoId, arquivo, tipoDocumento, descricao);
            redirectAttributes.addFlashAttribute("sucesso", "Anexo vinculado à movimentação com sucesso!");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/movimentacoes";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        AnexoPatrimonial anexo = anexoService.buscarPorId(id);
        Resource resource = anexoService.carregarArquivoComoRecurso(anexo);

        String contentType = (anexo.getTipoMime() != null && !anexo.getTipoMime().isBlank())
                ? anexo.getTipoMime()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + anexo.getNomeOriginal() + "\"")
                .body(resource);
    }

    @PostMapping("/excluir/{id}")
    public String excluir(@PathVariable Long id,
                          @RequestParam(value = "redirectTo", required = false) String redirectTo,
                          RedirectAttributes redirectAttributes) {
        try {
            anexoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Anexo excluído com sucesso.");
        } catch (RegraNegocioException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        if (redirectTo != null && redirectTo.startsWith("/") && !redirectTo.startsWith("//")) {
            return "redirect:" + redirectTo;
        }
        return "redirect:/equipamentos";
    }
}