package br.com.multivix.projetobimestre2.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "responsavel")
public class Responsavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do servidor responsável é obrigatório.")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nome;

    @NotBlank(message = "A matrícula funcional é obrigatória.")
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String matricula;

    @Size(max = 80)
    @Column(length = 80)
    private String cargo;

    @Email(message = "Informe um e-mail institucional válido.")
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @NotNull(message = "Selecione o setor de lotação do responsável.")
    @ManyToOne
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    @Column(nullable = false)
    private boolean ativo = true;

    public Responsavel() {
    }

    public Responsavel(String nome, String matricula, String cargo, String email, Setor setor) {
        this.nome = nome;
        this.matricula = matricula;
        this.cargo = cargo;
        this.email = email;
        this.setor = setor;
        this.ativo = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Setor getSetor() { return setor; }
    public void setSetor(Setor setor) { this.setor = setor; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public String getNomeSetor() {
        return setor != null ? setor.getNome() : "-";
    }

    public String getDescricaoSetorResponsavel() {
        String prefixo = setor != null ? setor.getSigla() + " - " + setor.getNome() : "Sem setor";
        return prefixo + " | Resp.: " + nome + " (Mat. " + matricula + ")";
    }
}