package br.com.sistema.acompanhamento.historico.estrutura;

import java.util.Locale;

public enum CriterioOrdenacao {
    DATA, CARGA, REPETICOES, VOLUME;

    public static CriterioOrdenacao de(String texto) {
        if (texto != null) {
            try {
                return valueOf(texto.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                // nao existe, cai no erro la embaixo
            }
        }
        throw new IllegalArgumentException("ordenarPor inválido. Use: data, carga, repeticoes ou volume.");
    }

    public String comoTexto() {
        return name().toLowerCase(Locale.ROOT);
    }
}
