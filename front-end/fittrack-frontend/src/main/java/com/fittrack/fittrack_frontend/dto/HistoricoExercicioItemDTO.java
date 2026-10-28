package com.fittrack.fittrack_frontend.dto;

/**
 * Registro resumido de uma execução, usado na lista e no gráfico de evolução do exercício.
 *
 * @param dataTreino data apresentada para a sessão
 * @param descricaoSeries resumo textual da quantidade de séries e repetições
 * @param carga carga associada ao registro em quilogramas
 * @param volumeCalculado volume agregado da execução
 * @param statusEvolucao classificação textual da evolução em relação ao histórico
 */
public record HistoricoExercicioItemDTO(
    /** Data da sessão correspondente. */
    String dataTreino,
    /** Resumo das séries, por exemplo, "4×10". */
    String descricaoSeries,
    /** Carga de referência registrada para a sessão. */
    double carga,
    /** Volume calculado para a execução. */
    double volumeCalculado,
    /** Estado textual usado para escolher o estilo do selo de evolução. */
    String statusEvolucao
) {}
