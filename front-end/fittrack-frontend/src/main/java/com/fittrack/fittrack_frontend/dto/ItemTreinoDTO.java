package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/** Tabela ITEM_TREINO (com o Exercicio associado e as séries já executadas). */
public record ItemTreinoDTO(
    Long id,
    Integer ordem,
    Integer seriesPrevistas,
    Integer repsPrevistas,
    Double cargaPrevista,
    Boolean concluido,
    ExercicioDTO exercicio,
    List<SerieExecutadaDTO> seriesRealizadas
) {}
