package com.fittrack.fittrack_frontend.dto;

/**
 * Ponto mensal da série de frequência exibida no detalhe de grupo muscular.
 *
 * @param mes rótulo curto do mês para o eixo horizontal
 * @param qtdTreinos quantidade de treinos contabilizados naquele mês
 */
public record FrequenciaMesDTO(
    /** Nome ou abreviação do mês. */
    String mes,
    /** Total de treinos realizados no mês. */
    Integer qtdTreinos
) {}
