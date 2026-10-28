package com.fittrack.fittrack_frontend.dto;

/**
 * Representação de exercício transportada entre a API e as telas de catálogo, histórico e recordes.
 *
 * @param id identificador do exercício no domínio
 * @param nome nome apresentado nos cards e detalhes
 * @param grupoMuscular grupo muscular associado ao exercício
 * @param descricao descrição informativa do movimento
 * @param ativo indica se o exercício está disponível no catálogo
 * @param recordeCarga maior carga registrada para o exercício
 * @param recordeRepsParaCarga repetições do recorde de carga correspondente
 * @param recordeVolumeSessao maior volume registrado em uma sessão
 * @param totalVezesRealizado quantidade de sessões em que o exercício foi realizado
 */
public record ExercicioDTO(
    /** Chave do exercício no back-end. */
    Long id,
    /** Nome legível do movimento. */
    String nome,
    /** Código do grupo muscular, convertido em rótulo pela UI quando necessário. */
    String grupoMuscular,
    /** Texto descritivo opcional do exercício. */
    String descricao,
    /** Estado de disponibilidade do exercício. */
    Boolean ativo,
    /** Maior peso absoluto registrado. */
    Double recordeCarga,
    /** Repetições realizadas na carga de recorde. */
    Integer recordeRepsParaCarga,
    /** Maior volume de uma sessão para este exercício. */
    Double recordeVolumeSessao,
    /** Número de vezes em que há registro de realização. */
    Integer totalVezesRealizado
) {}
