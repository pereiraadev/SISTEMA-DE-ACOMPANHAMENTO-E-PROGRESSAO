package com.fittrack.fittrack_frontend.dto;

public record HistoricoExercicioItemDTO(
    String dataTreino,
    String descricaoSeries, // Ex: "4×10"
    double carga,
    double volumeCalculado,
    String statusEvolucao // "Aumento de carga", "Evolução por reps", "Manutenção", "Redução"
) {}
