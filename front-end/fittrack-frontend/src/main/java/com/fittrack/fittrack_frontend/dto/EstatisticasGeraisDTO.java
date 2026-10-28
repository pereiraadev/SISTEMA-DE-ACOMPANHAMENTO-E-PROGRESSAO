package com.fittrack.fittrack_frontend.dto;

/**
 * Indicadores consolidados que alimentam os cartões de resumo do dashboard.
 *
 * @param totalTreinos quantidade de treinos contabilizados
 * @param totalExercicios quantidade de exercícios distintos realizados
 * @param totalSeries quantidade total de séries registradas
 * @param volumeTotalAcumulado volume histórico acumulado em quilogramas
 */
public record EstatisticasGeraisDTO(
    /** Número de sessões concluídas. */
    int totalTreinos,
    /** Exercícios que possuem registros para o praticante. */
    int totalExercicios,
    /** Séries computadas no histórico do praticante. */
    int totalSeries,
    /** Soma de carga multiplicada por repetições em todas as séries. */
    double volumeTotalAcumulado
) {}
