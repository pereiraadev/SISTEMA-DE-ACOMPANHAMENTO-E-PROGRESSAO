package com.fittrack.fittrack_frontend.dto;

/**
 * Resultado da consulta de desempenho de um grupo muscular para a aba de métricas.
 *
 * @param grupoMuscular código do grupo retornado pela API
 * @param volumeSemanal volume semanal agregado em quilogramas
 * @param percentualVariacao texto pronto para a indicação visual de variação
 * @param isAumento indica se a variação deve ser apresentada como positiva
 */
public record DesempenhoGrupoDTO(
    /** Grupo muscular ao qual os indicadores pertencem. */
    String grupoMuscular,
    /** Soma semanal do volume realizado pelo praticante. */
    double volumeSemanal,
    /** Rótulo de variação, por exemplo, "+12% vol". */
    String percentualVariacao, // Ex: "+12% vol" ou "-3% vol"
    /** Orienta a cor/estilo do indicador de variação na interface. */
    boolean isAumento
) {}
