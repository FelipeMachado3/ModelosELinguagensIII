// abstract class
abstract class MeioDePagamento {

    protected double valor;

    public MeioDePagamento(double valor) {
        this.valor = valor;
    }

    // metodo abstrato
    public abstract void pagar();
}

// subclasse concreta implementando o metodo abstrato
class Cartao extends MeioDePagamento {

    public Cartao(double valor) {
        super(valor); // construtor chamando super(valor)
    }

    @Override
    public void pagar() {
        System.out.println("Pagamento de R$ " + valor + " realizado com cartao.");
    }
}

public class Exemplo03 {
    public static void main(String[] args) {
        Cartao cartao = new Cartao(250.00);
        cartao.pagar();
    }
}