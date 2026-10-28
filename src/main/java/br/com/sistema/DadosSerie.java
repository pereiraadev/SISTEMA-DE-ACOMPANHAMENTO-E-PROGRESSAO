package br.com.sistema.acompanhamento;

// Guarda a carga e as repeticoes de uma serie.
public class DadosSerie {

    private double carga;
    private int repeticoes;

    // Recebe os dados da serie.
    public DadosSerie(double carga, int repeticoes) {
        this.carga = carga;
        this.repeticoes = repeticoes;
    }

    // Devolve a carga utilizada.
    public double getCarga() {
        return carga;
    }

    // Devolve a quantidade de repeticoes.
    public int getRepeticoes() {
        return repeticoes;
    }
}
