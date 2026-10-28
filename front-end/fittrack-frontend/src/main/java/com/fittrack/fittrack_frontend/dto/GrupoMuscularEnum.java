package com.fittrack.fittrack_frontend.dto;

import java.text.Normalizer;
import java.util.Optional;

/**
 * Códigos estáveis de grupo muscular associados aos rótulos e ícones da interface.
 * Os DTOs transportam o código como texto; esta enum normaliza esse texto para apresentação.
 */
public enum GrupoMuscularEnum {
    PEITO("Peito", "/icons/Frame (1).png"),
    COSTAS("Costas", "/icons/Frame (2).png"),
    PERNAS("Pernas", "/icons/Frame (3).png"),
    OMBROS("Ombros", "/icons/Frame (4).png"),
    BRACOS("Braços", "/icons/dumbbell.png");

    /** Nome localizado usado em rótulos e títulos de tela. */
    private final String rotulo;
    /** Caminho do recurso visual associado ao grupo. */
    private final String icone;

    /**
     * Associa ao código do enum os recursos específicos de apresentação.
     *
     * @param rotulo nome amigável mostrado ao usuário
     * @param icone caminho do ícone incluído nos recursos da aplicação
     */
    GrupoMuscularEnum(String rotulo, String icone) {
        this.rotulo = rotulo;
        this.icone = icone;
    }

    /** @return rótulo localizado para a interface */
    public String rotulo() { return rotulo; }

    /** @return caminho do ícone do grupo */
    public String icone() { return icone; }

    /**
     * Converte texto retornado pela API em um valor conhecido, ignorando caixa e acentos.
     *
     * @param valor texto do grupo recebido da API
     * @return grupo correspondente ou vazio quando o valor é nulo/desconhecido
     */
    public static Optional<GrupoMuscularEnum> from(String valor) {
        if (valor == null || valor.isBlank()) return Optional.empty();
        String normalizado = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toUpperCase();
        if (normalizado.equals("PEITORAL")) normalizado = "PEITO";
        for (GrupoMuscularEnum g : values()) {
            if (g.name().equals(normalizado)) return Optional.of(g);
        }
        return Optional.empty();
    }

    /**
     * Resolve o nome para exibição sem ocultar códigos desconhecidos enviados pela API.
     *
     * @param valor texto original do grupo
     * @return rótulo conhecido, texto original ou "Geral" quando nulo
     */
    public static String rotuloDe(String valor) {
        return from(valor).map(GrupoMuscularEnum::rotulo).orElse(valor != null ? valor : "Geral");
    }
}
