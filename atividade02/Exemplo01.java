package atividade02;

// EXEMPLO 01 - Lançar e capturar uma exceção

// CLASSE
class ContaBancaria {

    // ATRIBUTO
    // ENCAPSULAMENTO: protected permite acesso pelas subclasses
    protected double saldo;

    public void depositar(double valor) {
        saldo += valor;
    }

    public void sacar(double valor) {

        // THROW: lança uma exceção quando o saldo é insuficiente
        if (valor > saldo) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        saldo -= valor;
    }
}


// CLASSE PRINCIPAL
public class Exemplo01 {

    public static void main(String[] args) {

        // CRIAÇÃO DE OBJETO
        ContaBancaria conta = new ContaBancaria();

        conta.depositar(100.00);

        // TRY/CATCH: tenta executar o saque e captura o erro
        try {
            conta.sacar(500.00);

        // EXCEÇÃO UNCHECKED:
        // IllegalArgumentException não precisa obrigatoriamente
        // ser tratada pelo compilador.
        } catch (IllegalArgumentException excecao) {

            System.out.println("Erro: " + excecao.getMessage());
        }

        System.out.println("Saldo final: " + conta.saldo);
    }
}
