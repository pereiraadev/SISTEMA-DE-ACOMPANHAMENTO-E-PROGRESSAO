package com.fittrack.fittrack_frontend.dto;

import java.text.Normalizer;
import java.util.Optional;

/**
 * Espelho do GrupoMuscularEnum do diagrama UML (PEITO, COSTAS, PERNAS, OMBROS, BRACOS).
 * Os DTOs trafegam o grupo como String; esta enum serve para exibição (rótulo/ícone) na UI.
 */
public enum GrupoMuscularEnum {
    PEITO("Peito", "/icons/Frame (1).png"),
    COSTAS("Costas", "/icons/Frame (2).png"),
    PERNAS("Pernas", "/icons/Frame (3).png"),
    OMBROS("Ombros", "/icons/Frame (4).png"),
    BRACOS("Braços", "/icons/dumbbell.png");

    private final String rotulo;
    private final String icone;

    GrupoMuscularEnum(String rotulo, String icone) {
        this.rotulo = rotulo;
        this.icone = icone;
    }

    public String rotulo() { return rotulo; }

    public String icone() { return icone; }

    /** Converte o texto vindo da API (ex.: "PEITO", "Peitoral", "Braços") para a enum. */
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

    /** Rótulo amigável para a UI; devolve o texto original se não mapear para a enum. */
    public static String rotuloDe(String valor) {
        return from(valor).map(GrupoMuscularEnum::rotulo).orElse(valor != null ? valor : "Geral");
    }
}
