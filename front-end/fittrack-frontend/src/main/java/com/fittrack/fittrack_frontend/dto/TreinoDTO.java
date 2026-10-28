package com.fittrack.fittrack_frontend.dto;

import java.util.List;

public record TreinoDTO(
    Long id,
    String identificacao,
    boolean isModelo,
    String dataTreino,
    String status,
    String iniciadoEm,
    String finalizadoEm,
    Long treinoOrigemId,
    List<ItemTreinoDTO> itensTreino
) {}
