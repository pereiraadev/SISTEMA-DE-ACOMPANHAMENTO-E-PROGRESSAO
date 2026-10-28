package br.com.sistema.acompanhamento.comparativo;

import java.math.BigDecimal;

// resultado de comparar o valor atual com o anterior
public enum Variacao {
    AUMENTO, REDUCAO, MANUTENCAO;

    public static Variacao de(BigDecimal atual, BigDecimal anterior) {
        return de(atual.compareTo(anterior));
    }

    public static Variacao de(int atual, int anterior) {
        return de(Integer.compare(atual, anterior));
    }

    private static Variacao de(int comparacao) {
        if (comparacao > 0) {
            return AUMENTO;
        }
        if (comparacao < 0) {
            return REDUCAO;
        }
        return MANUTENCAO;
    }
}
