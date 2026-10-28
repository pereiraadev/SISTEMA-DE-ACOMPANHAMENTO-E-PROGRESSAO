package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/** Sessão de treino em andamento (Treino com is_modelo = false e status EM_ANDAMENTO). */
public record TreinoAtivoDTO(
    Long treinoId,
    String identificacao,
    long segundosDecorridos,
    List<ItemTreinoDTO> itens
) {}
