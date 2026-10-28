package com.fittrack.fittrack_frontend.dto;

import java.util.List;

public record RegistroHistoricoDTO(
    Long treinoId,
    long segundosDecorridos,
    List<SerieExecutadaDTO> seriesExecutadas
) {}