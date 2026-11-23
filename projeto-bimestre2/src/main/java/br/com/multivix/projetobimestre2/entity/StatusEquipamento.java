package br.com.multivix.projetobimestre2.entity;

public enum StatusEquipamento {
    DISPONIVEL("Disponível", "bg-success"),
    EM_USO("Em uso", "bg-primary"),
    EMPRESTADO("Emprestado", "bg-info"),
    MANUTENCAO("Em manutenção", "bg-warning text-dark"),
    BAIXADO("Baixado", "bg-secondary");

    private final String descricao;
    private final String badgeClass;

    StatusEquipamento(String descricao, String badgeClass) {
        this.descricao = descricao;
        this.badgeClass = badgeClass;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getBadgeClass() {
        return badgeClass;
    }

    public boolean isDisponivelParaCautela() {
        return this == DISPONIVEL;
    }
}