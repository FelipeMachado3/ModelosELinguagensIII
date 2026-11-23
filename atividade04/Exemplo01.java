// abstract class: nao pode ser instanciada diretamente
abstract class MeioDePagamento {

    protected double valor;

    // construtor da classe abstrata
    public MeioDePagamento(double valor) {
        this.valor = valor;
    }

    // metodo abstrato: termina com ; e nao tem corpo
    public abstract void pagar();
}

public class Exemplo01 {
    public static void main(String[] args) {
        System.out.println("Teste");
    }
}