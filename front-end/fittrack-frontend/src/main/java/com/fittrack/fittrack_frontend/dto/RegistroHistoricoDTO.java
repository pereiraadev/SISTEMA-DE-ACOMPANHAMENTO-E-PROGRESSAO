package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/** Payload enviado ao finalizar o treino (status passa a CONCLUIDO no back-end). */
public record RegistroHistoricoDTO(
    Long treinoId,
    long segundosDecorridos,
    List<SerieExecutadaDTO> seriesExecutadas
) {}
