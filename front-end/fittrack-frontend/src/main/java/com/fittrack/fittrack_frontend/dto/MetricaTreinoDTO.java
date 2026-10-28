package com.fittrack.fittrack_frontend.dto;

public record MetricaTreinoDTO(
    Long treinoId,
    String identificacao,
    String ultimaSessaoData,
    int consistenciaPercentual,
    int totalSessoes
) {}
