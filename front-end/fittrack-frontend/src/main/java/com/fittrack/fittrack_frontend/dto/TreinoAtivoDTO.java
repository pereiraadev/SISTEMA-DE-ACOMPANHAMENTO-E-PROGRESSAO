package com.fittrack.fittrack_frontend.dto;

import java.util.List;

public record TreinoAtivoDTO(
    Long treinoId,
    Long itemTreinoAtualId,
    String identificacao,
    long segundosDecorridos,
    List<ItemTreinoDTO> itens
) {}

