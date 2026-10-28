package com.fittrack.fittrack_frontend.dto;

import java.util.List;

public record ItemTreinoDTO(
    Long id,
    Long exercicioId,
    String nomeExercicio,
    String grupoMuscular,
    int ordem,
    int seriesPrevistas,
    int repsPrevistas,
    double cargaPrevista,
    boolean concluido,
    String referenciaTreinoAnterior, // Ex: "4×10 - 80kg"[cite: 19]
    List<SerieExecutadaDTO> seriesRealizadas
) {}
