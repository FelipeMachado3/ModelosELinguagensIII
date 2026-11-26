package br.com.multivix.projetobimestre2.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "anexos_patrimoniais")
public class AnexoPatrimonial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_original", nullable = false, length = 255)
    private String nomeOriginal;

    @Column(name = "nome_armazenado", nullable = false, length = 255)
    private String nomeArmazenado;

    @Column(name = "tipo_mime", length = 120)
    private String tipoMime;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @Column(name = "tipo_documento", nullable = false, length = 50)
    private String tipoDocumento;

    @Column(length = 300)
    private String descricao;

    @Column(name = "data_upload", nullable = false)
    private LocalDateTime dataUpload;

    @Column(name = "enviado_por_email", length = 120)
    private String enviadoPorEmail;

    @Column(name = "enviado_por_nome", length = 120)
    private String enviadoPorNome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipamento_id")
    private Equipamento equipamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movimentacao_id")
    private Movimentacao movimentacao;

    @PrePersist
    public void prePersist() {
        if (this.dataUpload == null) {
            this.dataUpload = LocalDateTime.now();
        }
    }

    public boolean isBloqueadoExclusao() {
        if (movimentacao != null && !movimentacao.isAtiva()) {
            return true;
        }
        if (equipamento != null && equipamento.getSituacao() == StatusEquipamento.BAIXADO) {
            return true;
        }
        return false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomeOriginal() { return nomeOriginal; }
    public void setNomeOriginal(String nomeOriginal) { this.nomeOriginal = nomeOriginal; }
    public String getNomeArmazenado() { return nomeArmazenado; }
    public void setNomeArmazenado(String nomeArmazenado) { this.nomeArmazenado = nomeArmazenado; }
    public String getTipoMime() { return tipoMime; }
    public void setTipoMime(String tipoMime) { this.tipoMime = tipoMime; }
    public long getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }
    public String getEnviadoPorEmail() { return enviadoPorEmail; }
    public void setEnviadoPorEmail(String enviadoPorEmail) { this.enviadoPorEmail = enviadoPorEmail; }
    public String getEnviadoPorNome() { return enviadoPorNome; }
    public void setEnviadoPorNome(String enviadoPorNome) { this.enviadoPorNome = enviadoPorNome; }
    public Equipamento getEquipamento() { return equipamento; }
    public void setEquipamento(Equipamento equipamento) { this.equipamento = equipamento; }
    public Movimentacao getMovimentacao() { return movimentacao; }
    public void setMovimentacao(Movimentacao movimentacao) { this.movimentacao = movimentacao; }
}