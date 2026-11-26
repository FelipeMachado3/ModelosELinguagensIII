package br.com.multivix.projetobimestre2.entity;

public enum PerfilAcesso {
    MANAGER("Manager (Gestor Geral)", "Acesso irrestrito a todos os módulos, configurações, aprovações críticas e baixas patrimoniais"),
    ADMIN("Administrador Geral", "Gerencia cadastros estruturais (usuários, setores, responsáveis e bens) e supervisão operacional"),
    SECRETARIA("Gestor de Secretaria", "Executa cadastros de bens, abertura e encerramento de cautelas e emissão de termos"),
    OPERADOR("Operador de Almoxarifado", "Realiza retiradas, devoluções, prorrogações, anexos e conferência de inventário"),
    AUDITOR("Consulta / Auditoria", "Acesso exclusivo de leitura e download a registros, anexos, histórico e logs de auditoria");

    private final String descricao;
    private final String resumoPermissao;

    PerfilAcesso(String descricao, String resumoPermissao) {
        this.descricao = descricao;
        this.resumoPermissao = resumoPermissao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getResumoPermissao() {
        return resumoPermissao;
    }
}