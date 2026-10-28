package br.com.sistema.acompanhamento;

import java.util.Locale;

public enum DirecaoOrdenacao {
    ASC, DESC;

    public static DirecaoOrdenacao de(String texto) {
        if (texto != null) {
            try {
                return valueOf(texto.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                // Nao existe, cai no erro la embaixo.
            }
        }
        throw new IllegalArgumentException("direcao inválida. Use: asc ou desc.");
    }

    public boolean decrescente() {
        return this == DESC;
    }

    public String comoTexto() {
        return name().toLowerCase(Locale.ROOT);
    }
}
