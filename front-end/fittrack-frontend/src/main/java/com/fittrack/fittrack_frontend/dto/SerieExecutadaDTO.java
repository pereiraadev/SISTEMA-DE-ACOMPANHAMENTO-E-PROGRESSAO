package com.fittrack.fittrack_frontend.dto;

public record SerieExecutadaDTO(
    Long id,
    int numeroSerie,
    double cargaUtilizada,
    int repeticoesRealizadas,
    String horarioConclusao,
    double volumeCalculado,
    boolean concluida
) {}