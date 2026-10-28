package com.fittrack.fittrack_frontend.dto;

public record EstatisticasGeraisDTO(
    int totalTreinos,
    int totalExercicios,
    int totalSeries,
    double volumeTotalAcumulado
) {}
