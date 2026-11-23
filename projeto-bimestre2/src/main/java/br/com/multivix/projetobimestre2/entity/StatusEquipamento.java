
package br.com.multivix.projetobimestre2.entity;

public enum StatusEquipamento {
    DISPONIVEL("Disponível"),
    EM_USO("Em uso"),
    EMPRESTADO("Emprestado"),
    MANUTENCAO("Em manutenção"),
    BAIXADO("Baixado");

    private final String descricao;

    StatusEquipamento(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isDisponivelParaCautela() {
        return this == DISPONIVEL;
    }
}
