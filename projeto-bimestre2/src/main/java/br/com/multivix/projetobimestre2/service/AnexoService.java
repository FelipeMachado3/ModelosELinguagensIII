package br.com.multivix.projetobimestre2.service;

import br.com.multivix.projetobimestre2.entity.AnexoPatrimonial;
import br.com.multivix.projetobimestre2.entity.Equipamento;
import br.com.multivix.projetobimestre2.entity.Movimentacao;
import br.com.multivix.projetobimestre2.entity.Usuario;
import br.com.multivix.projetobimestre2.exception.RegraNegocioException;
import br.com.multivix.projetobimestre2.repository.AnexoPatrimonialRepository;
import br.com.multivix.projetobimestre2.repository.EquipamentoRepository;
import br.com.multivix.projetobimestre2.repository.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AnexoService {

    public static final long TAMANHO_MAXIMO_BYTES = 10L * 1024L * 1024L;
    public static final int LIMITE_ANEXOS_POR_REGISTRO = 5;

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of(
            ".pdf", ".jpg", ".jpeg", ".png", ".docx", ".xlsx"
    );

    private static final Set<String> TIPOS_MIME_PERMITIDOS = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/octet-stream"
    );

    private final Path diretorioUpload;
    private final AnexoPatrimonialRepository anexoRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final AuditoriaService auditoriaService;

    public AnexoService(@Value("${app.upload.dir:uploads/anexos}") String uploadDir,
                        AnexoPatrimonialRepository anexoRepository,
                        EquipamentoRepository equipamentoRepository,
                        MovimentacaoRepository movimentacaoRepository,
                        AuditoriaService auditoriaService) {
        this.diretorioUpload = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.anexoRepository = anexoRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<AnexoPatrimonial> listarPorEquipamento(Long equipamentoId) {
        return anexoRepository.findByEquipamentoIdOrderByDataUploadDesc(equipamentoId);
    }

    public List<AnexoPatrimonial> listarPorMovimentacao(Long movimentacaoId) {
        return anexoRepository.findByMovimentacaoIdOrderByDataUploadDesc(movimentacaoId);
    }

    public AnexoPatrimonial buscarPorId(Long id) {
        return anexoRepository.findById(id)
                .orElseThrow(() -> new RegraNegocioException("Anexo não encontrado."));
    }

    public boolean possuiEvidenciaAvariaNaMovimentacao(Long movimentacaoId) {
        if (movimentacaoId == null) return false;
        return anexoRepository.existsByMovimentacaoIdAndTipoDocumentoIgnoreCase(movimentacaoId, "EVIDENCIA_AVARIA")
                || anexoRepository.countByMovimentacaoId(movimentacaoId) > 0;
    }

    public boolean possuiLaudoBaixaNoEquipamento(Long equipamentoId) {
        if (equipamentoId == null) return false;
        return anexoRepository.existsByEquipamentoIdAndTipoDocumentoIgnoreCase(equipamentoId, "LAUDO_BAIXA");
    }

    @Transactional
    public AnexoPatrimonial anexarAoEquipamento(Long equipamentoId,
                                               MultipartFile arquivo,
                                               String tipoDocumento,
                                               String descricao) {
        Equipamento equipamento = equipamentoRepository.findById(equipamentoId)
                .orElseThrow(() -> new RegraNegocioException("Equipamento não encontrado para anexar documento."));

        if (anexoRepository.countByEquipamentoId(equipamentoId) >= LIMITE_ANEXOS_POR_REGISTRO) {
            throw new RegraNegocioException("Limite de " + LIMITE_ANEXOS_POR_REGISTRO + " anexos por equipamento atingido.");
        }

        String nomeArmazenado = validarESalvarArquivoFisico(arquivo);
        String tipoDocNormalizado = (tipoDocumento != null && !tipoDocumento.isBlank())
                ? tipoDocumento.trim().toUpperCase(Locale.ROOT)
                : "DOCUMENTO_BEM";

        AnexoPatrimonial anexo = criarEntidadeAnexo(arquivo, nomeArmazenado, tipoDocNormalizado, descricao);
        anexo.setEquipamento(equipamento);

        AnexoPatrimonial salvo = anexoRepository.save(anexo);
        auditoriaService.registrar(
                "ANEXOS",
                "UPLOAD_EQUIPAMENTO",
                "AnexoPatrimonial",
                salvo.getId(),
                equipamento.getPatrimonio(),
                "Anexo '" + salvo.getNomeOriginal() + "' (" + tipoDocNormalizado + ") vinculado ao patrimônio #" + equipamento.getPatrimonio()
        );
        return salvo;
    }

    @Transactional
    public AnexoPatrimonial anexarAMovimentacao(Long movimentacaoId,
                                                MultipartFile arquivo,
                                                String tipoDocumento,
                                                String descricao) {
        Movimentacao movimentacao = movimentacaoRepository.findById(movimentacaoId)
                .orElseThrow(() -> new RegraNegocioException("Movimentação não encontrada para anexar documento."));

        if (anexoRepository.countByMovimentacaoId(movimentacaoId) >= LIMITE_ANEXOS_POR_REGISTRO) {
            throw new RegraNegocioException("Limite de " + LIMITE_ANEXOS_POR_REGISTRO + " anexos por movimentação atingido.");
        }

        String nomeArmazenado = validarESalvarArquivoFisico(arquivo);
        String tipoDocNormalizado = (tipoDocumento != null && !tipoDocumento.isBlank())
                ? tipoDocumento.trim().toUpperCase(Locale.ROOT)
                : "TERMO_CAUTELA";

        AnexoPatrimonial anexo = criarEntidadeAnexo(arquivo, nomeArmazenado, tipoDocNormalizado, descricao);
        anexo.setMovimentacao(movimentacao);
        if (movimentacao.getEquipamento() != null) {
            anexo.setEquipamento(movimentacao.getEquipamento());
        }

        AnexoPatrimonial salvo = anexoRepository.save(anexo);
        String refPatrimonio = movimentacao.getEquipamento() != null ? movimentacao.getEquipamento().getPatrimonio() : null;
        auditoriaService.registrar(
                "ANEXOS",
                "UPLOAD_MOVIMENTACAO",
                "AnexoPatrimonial",
                salvo.getId(),
                refPatrimonio,
                "Anexo '" + salvo.getNomeOriginal() + "' (" + tipoDocNormalizado + ") vinculado ao termo " + movimentacao.getNumeroTermo()
        );
        return salvo;
    }

    public Resource carregarArquivoComoRecurso(AnexoPatrimonial anexo) {
        try {
            Path arquivoPath = diretorioUpload.resolve(anexo.getNomeArmazenado()).normalize();
            if (!arquivoPath.startsWith(diretorioUpload)) {
                throw new RegraNegocioException("Caminho de arquivo inválido.");
            }
            Resource resource = new UrlResource(arquivoPath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RegraNegocioException("Arquivo físico não encontrado no servidor.");
            }
            return resource;
        } catch (RegraNegocioException e) {
            throw e;
        } catch (Exception e) {
            throw new RegraNegocioException("Não foi possível ler o arquivo anexado.");
        }
    }

    @Transactional
    public void excluir(Long anexoId) {
        AnexoPatrimonial anexo = buscarPorId(anexoId);

        if (anexo.isBloqueadoExclusao()) {
            throw new RegraNegocioException(
                    "Este anexo pertence a um registro encerrado ou bem baixado e não pode ser excluído para preservar a auditoria."
            );
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String emailLogado = auth != null ? auth.getName() : "";
        boolean possuiPerfilGestao = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER")
                        || a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SECRETARIA"));

        boolean ehAutor = anexo.getEnviadoPorEmail() != null
                && anexo.getEnviadoPorEmail().equalsIgnoreCase(emailLogado);

        if (!ehAutor && !possuiPerfilGestao) {
            throw new RegraNegocioException("Apenas o autor do envio ou perfis de gestão podem excluir este anexo.");
        }

        String patrimonioRef = anexo.getEquipamento() != null ? anexo.getEquipamento().getPatrimonio() : null;
        String nomeOrig = anexo.getNomeOriginal();
        String nomeArm = anexo.getNomeArmazenado();

        anexoRepository.delete(anexo);

        try {
            Path arquivoPath = diretorioUpload.resolve(nomeArm).normalize();
            if (arquivoPath.startsWith(diretorioUpload)) {
                Files.deleteIfExists(arquivoPath);
            }
        } catch (IOException ignored) {
            // Registro de banco já removido; evita falha por lock temporário de arquivo no sistema operacional
        }

        auditoriaService.registrar(
                "ANEXOS",
                "EXCLUIR_ANEXO",
                "AnexoPatrimonial",
                anexoId,
                patrimonioRef,
                "Anexo '" + nomeOrig + "' removido de registro em aberto."
        );
    }

    private String validarESalvarArquivoFisico(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("Selecione um arquivo válido para anexar.");
        }

        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new RegraNegocioException("O arquivo excede o limite máximo permitido de 10 MB.");
        }

        String nomeOriginal = arquivo.getOriginalFilename() != null
                ? Paths.get(arquivo.getOriginalFilename()).getFileName().toString()
                : "arquivo";

        String extensao = extrairExtensaoMinuscula(nomeOriginal);
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new RegraNegocioException(
                    "Formato de arquivo não permitido ('" + extensao + "'). Aceitos apenas: PDF, JPG, JPEG, PNG, DOCX e XLSX."
            );
        }

        String contentType = arquivo.getContentType() != null
                ? arquivo.getContentType().toLowerCase(Locale.ROOT)
                : "";
        if (!contentType.isBlank() && !TIPOS_MIME_PERMITIDOS.contains(contentType)) {
            throw new RegraNegocioException("Tipo de conteúdo (MIME) do arquivo não é permitido.");
        }

        try {
            Files.createDirectories(diretorioUpload);
            String nomeArmazenado = UUID.randomUUID() + extensao;
            Path destino = diretorioUpload.resolve(nomeArmazenado).normalize();
            if (!destino.startsWith(diretorioUpload)) {
                throw new RegraNegocioException("Nome de arquivo inválido.");
            }
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
            return nomeArmazenado;
        } catch (RegraNegocioException e) {
            throw e;
        } catch (IOException e) {
            throw new RegraNegocioException("Falha ao armazenar o arquivo no servidor.");
        }
    }

    private AnexoPatrimonial criarEntidadeAnexo(MultipartFile arquivo,
                                               String nomeArmazenado,
                                               String tipoDocumento,
                                               String descricao) {
        String nomeOriginal = arquivo.getOriginalFilename() != null
                ? Paths.get(arquivo.getOriginalFilename()).getFileName().toString()
                : nomeArmazenado;

        AnexoPatrimonial anexo = new AnexoPatrimonial();
        anexo.setNomeOriginal(nomeOriginal);
        anexo.setNomeArmazenado(nomeArmazenado);
        anexo.setTipoMime(arquivo.getContentType());
        anexo.setTamanhoBytes(arquivo.getSize());
        anexo.setTipoDocumento(tipoDocumento);
        anexo.setDescricao(descricao != null && !descricao.isBlank() ? descricao.trim() : null);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            anexo.setEnviadoPorEmail(usuario.getEmail());
            anexo.setEnviadoPorNome(usuario.getNome());
        } else if (auth != null) {
            anexo.setEnviadoPorEmail(auth.getName());
            anexo.setEnviadoPorNome(auth.getName());
        }

        return anexo;
    }

    private String extrairExtensaoMinuscula(String nomeArquivo) {
        int idx = nomeArquivo.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        return nomeArquivo.substring(idx).toLowerCase(Locale.ROOT);
    }
}