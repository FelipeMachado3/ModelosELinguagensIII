package br.com.multivix.projetobimestre2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Entity
@Table(name = "setor")
public class Setor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A sigla do setor é obrigatória.")
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String sigla;

    @NotBlank(message = "O nome do setor / unidade administrativa é obrigatório.")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @Size(max = 120)
    @Column(length = 120)
    private String localizacao;

    @Size(max = 30)
    @Column(length = 30)
    private String ramal;

    @Column(name = "tipo_unidade", length = 30)
    private String tipoUnidade = "SECRETARIA";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "secretaria_pai_id")
    private Setor secretariaPai;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "inventario_aberto", nullable = false)
    private boolean inventarioAberto = false;

    @Column(name = "responsavel_titular_id")
    private Long responsavelTitularId;

    @Column(name = "data_transferencia_responsavel")
    private LocalDate dataTransferenciaResponsavel;

    public Setor() {
    }

    public Setor(String sigla, String nome, String localizacao, String ramal) {
        this.sigla = sigla;
        this.nome = nome;
        this.localizacao = localizacao;
        this.ramal = ramal;
        this.tipoUnidade = "SECRETARIA";
        this.ativo = true;
        this.inventarioAberto = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    public String getRamal() { return ramal; }
    public void setRamal(String ramal) { this.ramal = ramal; }

    public String getTipoUnidade() {
        return tipoUnidade != null ? tipoUnidade : "SECRETARIA";
    }

    public void setTipoUnidade(String tipoUnidade) { this.tipoUnidade = tipoUnidade; }
    public Setor getSecretariaPai() { return secretariaPai; }
    public void setSecretariaPai(Setor secretariaPai) { this.secretariaPai = secretariaPai; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
    public boolean isInventarioAberto() { return inventarioAberto; }
    public void setInventarioAberto(boolean inventarioAberto) { this.inventarioAberto = inventarioAberto; }
    public Long getResponsavelTitularId() { return responsavelTitularId; }
    public void setResponsavelTitularId(Long responsavelTitularId) { this.responsavelTitularId = responsavelTitularId; }
    public LocalDate getDataTransferenciaResponsavel() { return dataTransferenciaResponsavel; }
    public void setDataTransferenciaResponsavel(LocalDate dataTransferenciaResponsavel) { this.dataTransferenciaResponsavel = dataTransferenciaResponsavel; }

    public String getIdentificacao() {
        if (secretariaPai != null) {
            return secretariaPai.getSigla() + " / " + sigla + " - " + nome;
        }
        return sigla + " - " + nome;
    }
}