package br.com.multivix.projetobimestre2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(name = "equipamento")
public class Equipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome/especificação técnica do equipamento é obrigatório.")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "O número de tombamento/patrimônio é obrigatório.")
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String patrimonio;

    @Size(max = 80)
    @Column(length = 80)
    private String marca;

    @Size(max = 80)
    @Column(length = 80)
    private String modelo;

    @Size(max = 80)
    @Column(name = "numero_serie", length = 80)
    private String numeroSerie;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(name = "data_aquisicao")
    private LocalDate dataAquisicao;

    @NotNull(message = "O estado de conservação é obrigatório.")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_conservacao", nullable = false, length = 30)
    private EstadoConservacao estadoConservacao = EstadoConservacao.BOM;

    @NotNull(message = "A situação patrimonial é obrigatória.")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusEquipamento situacao = StatusEquipamento.DISPONIVEL;

    @Column(name = "conferido_inventario", nullable = false)
    private boolean conferidoInventario = false;

    @NotNull(message = "Selecione a categoria do equipamento.")
    @ManyToOne
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    public Equipamento() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getPatrimonio() {
        return patrimonio;
    }

    public void setPatrimonio(String patrimonio) {
        this.patrimonio = patrimonio;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getNumeroSerie() {
        return numeroSerie;
    }

    public void setNumeroSerie(String numeroSerie) {
        this.numeroSerie = numeroSerie;
    }

    public LocalDate getDataAquisicao() {
        return dataAquisicao;
    }

    public void setDataAquisicao(LocalDate dataAquisicao) {
        this.dataAquisicao = dataAquisicao;
    }

    public EstadoConservacao getEstadoConservacao() {
        return estadoConservacao;
    }

    public void setEstadoConservacao(EstadoConservacao estadoConservacao) {
        this.estadoConservacao = estadoConservacao;
    }

    public StatusEquipamento getSituacao() {
        return situacao;
    }

    public void setSituacao(StatusEquipamento situacao) {
        this.situacao = situacao;
    }

    public boolean isConferidoInventario() {
        return conferidoInventario;
    }

    public void setConferidoInventario(boolean conferidoInventario) {
        this.conferidoInventario = conferidoInventario;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getIdentificacaoCompleta() {
        return "Tombamento #" + patrimonio + " - " + nome
                + (marca != null && !marca.isBlank() ? " (" + marca + ")" : "");
    }
}
