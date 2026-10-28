package com.fittrack.fittrack_frontend.dto;

/** Resultado de Praticante.consultarDesempenhoGrupoMuscular(grupo). */
public record DesempenhoGrupoDTO(
    String grupoMuscular,
    double volumeSemanal,
    String percentualVariacao, // Ex: "+12% vol" ou "-3% vol"
    boolean isAumento
) {}
