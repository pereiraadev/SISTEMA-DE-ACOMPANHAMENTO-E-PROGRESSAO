package br.com.sistema.acompanhamento.comparativo;

import java.util.ArrayList;

// Compara o desempenho de um exercicio entre dois treinos.
public class ComparativoDesempenho {

    // Recebe a carga e as repeticoes do treino anterior e do atual.
    // Retorna um texto dizendo se houve aumento, reducao ou manutencao.
    public String comparar(
            double cargaAnterior,
            int repeticoesAnteriores,
            double cargaAtual,
            int repeticoesAtuais) {

        // Primeiro verificamos se a carga mudou.
        if (cargaAtual > cargaAnterior) {
            return "AUMENTO_CARGA";
        }

        if (cargaAtual < cargaAnterior) {
            return "REDUCAO_CARGA";
        }

        // Se a carga for igual, comparamos as repeticoes.
        if (repeticoesAtuais > repeticoesAnteriores) {
            return "AUMENTO_REPETICOES";
        }

        if (repeticoesAtuais < repeticoesAnteriores) {
            return "REDUCAO_REPETICOES";
        }

        // Se nada mudou, o desempenho foi mantido.
        return "MANUTENCAO";
    }

    // Recebe uma lista de series e procura a maior carga.
    // Se houver cargas iguais, considera mais repeticoes.
    public DadosSerie buscarMelhorSerie(ArrayList<DadosSerie> series) {

        // Se nao houver series, nao existe resultado.
        if (series == null || series.isEmpty()) {
            return null;
        }

        DadosSerie melhor = series.get(0);

        // Percorre as series para encontrar a melhor.
        for (DadosSerie serie : series) {

            if (serie.getCarga() > melhor.getCarga()) {
                melhor = serie;

            } else if (serie.getCarga() == melhor.getCarga()
                    && serie.getRepeticoes() > melhor.getRepeticoes()) {
                melhor = serie;
            }
        }

        return melhor;
    }

    // Recebe as series de dois treinos e compara os resultados.
    // Retorna a diferenca de desempenho ou informa se nao ha dados.
    public String compararExercicios(
            ArrayList<DadosSerie> seriesAnteriores,
            ArrayList<DadosSerie> seriesAtuais) {

        // Procura a melhor serie de cada treino.
        DadosSerie anterior = buscarMelhorSerie(seriesAnteriores);
        DadosSerie atual = buscarMelhorSerie(seriesAtuais);

        // Nao podemos comparar se algum treino nao tiver series.
        if (anterior == null || atual == null) {
            return "SEM_DADOS";
        }

        // Aproveita o metodo de comparacao que ja criamos.
        return comparar(
                anterior.getCarga(),
                anterior.getRepeticoes(),
                atual.getCarga(),
                atual.getRepeticoes());
    }

}