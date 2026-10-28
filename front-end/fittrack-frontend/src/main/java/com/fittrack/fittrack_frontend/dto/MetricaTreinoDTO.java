package com.fittrack.fittrack_frontend.dto;

/**
 * Indicadores de consistência e recorrência para uma ficha de treino na aba de métricas.
 *
 * @param treinoId identificador da ficha no domínio
 * @param identificacao nome ou código legível da ficha
 * @param ultimaSessaoData data da sessão mais recente
 * @param consistenciaPercentual percentual usado para desenhar a barra de progresso
 * @param totalSessoes quantidade de sessões vinculadas à ficha
 */
public record MetricaTreinoDTO(
    /** Chave da ficha de treino. */
    Long treinoId,
    /** Nome mostrado no cartão da aba. */
    String identificacao,
    /** Data exibida como última realização. */
    String ultimaSessaoData,
    /** Valor percentual representado na barra de consistência. */
    int consistenciaPercentual,
    /** Total de sessões concluídas para a ficha. */
    int totalSessoes
) {}
