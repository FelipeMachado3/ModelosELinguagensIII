package atividade02;

// EXEMPLO 03 - Try-with-resources


// CLASSE
// IMPLEMENTA AutoCloseable para que o Java possa
// fechar o recurso automaticamente.
class ConexaoSimulada implements AutoCloseable {

    // CONSTRUTOR
    public ConexaoSimulada() {
        System.out.println("Conexão aberta.");
    }

    public void executar() {
        System.out.println("Executando operação...");
    }

    // SOBRESCRITA:
    // implementação do método close() da interface AutoCloseable
    @Override
    public void close() {
        System.out.println("Conexão fechada.");
    }
}


// CLASSE PRINCIPAL
public class Exemplo03 {

    public static void main(String[] args) {

        // TRY-WITH-RESOURCES:
        // a conexão será fechada automaticamente ao final do try.
        try (ConexaoSimulada conexao = new ConexaoSimulada()) {

            // USO DO RECURSO
            conexao.executar();
        }
    }
}
