package com.fittrack.fittrack_frontend.dto;

public record ExercicioDTO(
    Long id,
    String nome,
    String grupoMuscular,
    String descricao,
    boolean ativo,
    Double recordeCarga,
    Integer recordeRepsParaCarga,
    Double recordeVolumeSessao,
    Integer totalVezesRealizado
) {}
