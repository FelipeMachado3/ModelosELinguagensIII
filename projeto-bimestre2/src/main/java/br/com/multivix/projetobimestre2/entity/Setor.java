package br.com.multivix.projetobimestre2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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

    public Setor() {
    }

    public Setor(String sigla, String nome, String localizacao, String ramal) {
        this.sigla = sigla;
        this.nome = nome;
        this.localizacao = localizacao;
        this.ramal = ramal;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getLocalizacao() {
        return localizacao;
    }

    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getRamal() {
        return ramal;
    }

    public void setRamal(String ramal) {
        this.ramal = ramal;
    }

    public String getIdentificacao() {
        return sigla + " - " + nome;
    }
}
