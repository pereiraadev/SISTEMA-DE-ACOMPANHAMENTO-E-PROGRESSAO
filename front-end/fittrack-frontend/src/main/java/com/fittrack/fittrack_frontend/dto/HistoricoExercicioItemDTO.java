package com.fittrack.fittrack_frontend.dto;

public record HistoricoExercicioItemDTO(
    String dataTreino,
    String descricaoSeries, // Ex: "4×10"[cite: 18]
    double carga,
    double volumeCalculado,
    String statusEvolucao // "Aumento de carga", "Evolução por reps", "Manutenção", "Redução"[cite: 18]
) {}
