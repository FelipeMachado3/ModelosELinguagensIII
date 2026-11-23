package atividade02;

// EXEMPLO CHECKED - Checked Exception

// IMPORTAÇÃO DA CLASSE FileReader
import java.io.FileReader;

// IMPORTAÇÃO DA EXCEÇÃO CHECKED
import java.io.FileNotFoundException;


// CLASSE PRINCIPAL
public class ExemploChecked {

    public static void main(String[] args) {

        // TRY/CATCH:
        // FileReader pode gerar uma FileNotFoundException.
        try {

            // CRIAÇÃO DE OBJETO
            FileReader arquivo = new FileReader("dados.txt");

        // EXCEÇÃO CHECKED:
        // O compilador obriga o tratamento dessa exceção.
        } catch (FileNotFoundException e) {

            System.out.println("Arquivo não encontrado.");
        }
    }
}
