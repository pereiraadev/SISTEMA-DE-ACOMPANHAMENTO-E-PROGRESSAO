package com.fittrack.fittrack_frontend.dto;

/**
 * Tabela SERIE_EXECUTADA. volumeCalculado = cargaUtilizada * repeticoesRealizadas.
 * itemTreinoId (FK item_treino_id) permite ao back-end associar cada série ao seu ItemTreino
 * quando as séries são enviadas em lista plana no RegistroHistoricoDTO.
 */
public record SerieExecutadaDTO(
    Long id,
    Integer numeroSerie,
    Double cargaUtilizada,
    Integer repeticoesRealizadas,
    String horarioConclusao,
    Double volumeCalculado,
    boolean concluida,
    Long itemTreinoId
) {}
