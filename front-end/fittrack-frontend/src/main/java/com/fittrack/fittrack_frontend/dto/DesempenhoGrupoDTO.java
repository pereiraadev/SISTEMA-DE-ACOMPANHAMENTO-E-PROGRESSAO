package com.fittrack.fittrack_frontend.dto;

public record DesempenhoGrupoDTO(
    String grupoMuscular,
    double volumeSemanal,
    String percentualVariacao, // Ex: "+12% vol" ou "-3% vol"[cite: 15]
    boolean isAumento
) {}
