package com.fittrack.fittrack_frontend.dto;

/**
 * Parcela do volume de um grupo muscular atribuída a um exercício.
 *
 * @param nome nome do exercício usado no gráfico e na legenda
 * @param volumeKg volume agregado em quilogramas
 */
public record VolumeExercicioDTO(
    /** Rótulo da categoria de exercício no gráfico de grupo. */
    String nome,
    /** Valor representado pela fatia correspondente do gráfico. */
    Double volumeKg
) {}
