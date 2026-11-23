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
class Boleto extends MeioDePagamento {

    public Boleto(double valor) {
        super(valor); // construtor chamando super(valor)
    }

    @Override
    public void pagar() {
        System.out.println("Boleto de R$ " + valor + " gerado.");
    }
}

public class Exemplo04 {
    public static void main(String[] args) {
        Boleto boleto = new Boleto(80.00);
        boleto.pagar();
    }
}