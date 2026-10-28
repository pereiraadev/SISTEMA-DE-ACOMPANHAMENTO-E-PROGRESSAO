package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/**
 * Representação geral de uma ficha ou sessão de treino retornada pela API.
 *
 * @param id identificador do treino
 * @param identificacao nome ou identificação apresentada na interface
 * @param isModelo indica se é uma ficha modelo em vez de uma sessão executada
 * @param dataTreino data associada ao treino
 * @param status estado atual do treino no domínio
 * @param iniciadoEm data e hora de início da sessão
 * @param finalizadoEm data e hora de conclusão da sessão
 * @param treinoOrigemId identificador do modelo que originou a sessão, se houver
 * @param itensTreino exercícios e prescrições que compõem o treino
 */
public record TreinoDTO(
    /** Chave do treino no back-end. */
    Long id,
    /** Nome legível da ficha ou sessão. */
    String identificacao,
    /** Distingue ficha planejada de sessão realizada. */
    boolean isModelo,
    /** Data prevista ou realizada. */
    String dataTreino,
    /** Estado de negócio, como em andamento ou concluído. */
    String status,
    /** Instante em que a sessão foi iniciada. */
    String iniciadoEm,
    /** Instante em que a sessão foi finalizada. */
    String finalizadoEm,
    /** Ficha modelo de origem de uma sessão clonada. */
    Long treinoOrigemId,
    /** Itens associados à ficha ou sessão. */
    List<ItemTreinoDTO> itensTreino
) {}
