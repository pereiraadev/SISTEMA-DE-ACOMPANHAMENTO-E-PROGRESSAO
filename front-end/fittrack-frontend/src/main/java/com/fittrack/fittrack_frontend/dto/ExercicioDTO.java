package com.fittrack.fittrack_frontend.dto;

/** Tabela EXERCICIO. grupoMuscular mapeia GrupoMuscularEnum: PEITO, COSTAS, PERNAS, OMBROS, BRACOS. */
public record ExercicioDTO(
    Long id,
    String nome,
    String grupoMuscular,
    String descricao,
    Boolean ativo,
    Double recordeCarga,
    Integer recordeRepsParaCarga,
    Double recordeVolumeSessao,
    Integer totalVezesRealizado
) {}
