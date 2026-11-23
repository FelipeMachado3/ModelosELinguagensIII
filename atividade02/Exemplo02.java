package atividade02;

// EXEMPLO 02 - Exceção Customizada


// EXCEÇÃO CUSTOMIZADA
// UNCHECKED: herda de RuntimeException
class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(String mensagem) {

        // SUPER: chama o construtor da classe RuntimeException
        super(mensagem);
    }
}


// CLASSE
class ContaBancaria {

    // ATRIBUTO
    // ENCAPSULAMENTO: protected
    protected double saldo;

    public void depositar(double valor) {
        saldo += valor;
    }

    public void sacar(double valor) {

        // THROW:
        // lança nossa própria exceção quando o saldo é insuficiente
        if (valor > saldo) {
            throw new SaldoInsuficienteException("Saldo insuficiente");
        }

        saldo -= valor;
    }
}


// CLASSE PRINCIPAL
public class Exemplo02 {

    public static void main(String[] args) {

        // CRIAÇÃO DE OBJETO
        ContaBancaria conta = new ContaBancaria();

        conta.depositar(100.00);

        // TRY/CATCH:
        // captura a exceção customizada
        try {

            conta.sacar(500.00);

        } catch (SaldoInsuficienteException excecao) {

            System.out.println("Erro: " + excecao.getMessage());
        }

        System.out.println("Saldo final: " + conta.saldo);
    }
}
