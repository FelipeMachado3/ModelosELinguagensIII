package atividade01;

// ======================================================
// PARTE 1 - INTERFACE
// ======================================================

// INTERFACE: define um contrato que as classes devem seguir
interface MeioDePagamento {
    void pagar(double valor);
}

// CLASSE: Pix implementa a interface MeioDePagamento
class Pix implements MeioDePagamento {

    // SOBRESCRITA: implementação do método da interface
    @Override
    public void pagar(double valor) {
        System.out.println("Pagamento de R$ " + valor + " realizado via Pix.");
    }
}

// CLASSE: Cartao implementa a interface MeioDePagamento
class Cartao implements MeioDePagamento {

    // SOBRESCRITA: implementação do método da interface
    @Override
    public void pagar(double valor) {
        System.out.println("Pagamento de R$ " + valor + " realizado com cartão.");
    }
}

// CLASSE: Boleto implementa a interface MeioDePagamento
class Boleto implements MeioDePagamento {

    // SOBRESCRITA: implementação do método da interface
    @Override
    public void pagar(double valor) {
        System.out.println("Boleto de R$ " + valor + " gerado.");
    }
}


// ======================================================
// PARTE 2 - HERANÇA
// ======================================================

// CLASSE: ContaBancaria é a classe-pai
class ContaBancaria {

    // ATRIBUTO: armazena o saldo da conta
    // ENCAPSULAMENTO: protected permite acesso pelas subclasses
    protected double saldo;

    public void depositar(double valor) {
        saldo += valor;
        System.out.println("Depósito realizado. Saldo: R$ " + saldo);
    }
}

// HERANÇA: ContaPoupanca herda de ContaBancaria
class ContaPoupanca extends ContaBancaria {
}


// HERANÇA: ContaCorrente herda de ContaBancaria
class ContaCorrente extends ContaBancaria {

    // SOBRESCRITA: modifica o comportamento do método depositar
    @Override
    public void depositar(double valor) {

        // SUPER: chama o método depositar() da classe-pai
        // Desconta R$ 1,00 de taxa
        super.depositar(valor - 1.00);

        System.out.println("Foi descontada uma taxa de R$ 1,00.");
    }
}
public class Main {

    public static void main(String[] args) {

        // --------------------------------------------------
        // INTERFACE + POLIMORFISMO
        // --------------------------------------------------

        // POLIMORFISMO:
        // a variável é do tipo MeioDePagamento,
        // mas recebe um objeto Pix.
        // CRIAÇÃO DE OBJETO: new Pix()
        MeioDePagamento pagamento = new Pix();

        pagamento.pagar(100.00);

        // POLIMORFISMO:
        // a mesma variável agora recebe um objeto Cartao.
        // CRIAÇÃO DE OBJETO: new Cartao()
        pagamento = new Cartao();

        pagamento.pagar(250.00);

        // POLIMORFISMO:
        // a mesma variável agora recebe um objeto Boleto.
        // CRIAÇÃO DE OBJETO: new Boleto()
        pagamento = new Boleto();

        pagamento.pagar(80.00);


        // --------------------------------------------------
        // HERANÇA
        // --------------------------------------------------

        // CRIAÇÃO DE OBJETO:
        // cria uma conta poupança.
        // HERANÇA: ContaPoupanca herda de ContaBancaria.
        ContaPoupanca poupanca = new ContaPoupanca();

        poupanca.depositar(100.00);


        // CRIAÇÃO DE OBJETO:
        // cria uma conta corrente.
        // HERANÇA: ContaCorrente herda de ContaBancaria.
        ContaCorrente corrente = new ContaCorrente();

        corrente.depositar(100.00);
    }
}

