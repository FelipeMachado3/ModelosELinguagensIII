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
class Pix extends MeioDePagamento {

    public Pix(double valor) {
        super(valor); // construtor chamando super(valor)
    }

    @Override
    public void pagar() {
        System.out.println("Pagamento de R$ " + valor + " realizado via Pix.");
    }
}

public class Exemplo02 {
    public static void main(String[] args) {
        // instanciamos Pix, nunca MeioDePagamento
        MeioDePagamento pagamento = new Pix(100.00);
        pagamento.pagar();
    }
}