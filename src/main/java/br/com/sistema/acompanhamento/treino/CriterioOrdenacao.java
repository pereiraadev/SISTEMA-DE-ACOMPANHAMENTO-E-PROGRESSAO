package br.com.sistema.acompanhamento.treino;

// Criterios aceitos para ordenar o historico de treinos.
public enum CriterioOrdenacao {
    DATA, CARGA, REPETICOES, VOLUME;

    // Converte o texto recebido na requisicao (ex.: "carga"); lanca IllegalArgumentException se for invalido.
    public static CriterioOrdenacao de(String texto) {
        if (texto == null || texto.isBlank()) return DATA;
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Criterio de ordenacao invalido: " + texto
                    + " (use data, carga, repeticoes ou volume)");
        }
    }
}
