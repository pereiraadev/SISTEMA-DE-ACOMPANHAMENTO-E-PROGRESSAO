package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/**
 * Payload enviado ao encerrar uma sessão, contendo a duração e todas as séries concluídas.
 *
 * @param treinoId identificador da sessão ativa a finalizar
 * @param segundosDecorridos duração total medida pelo cronômetro da tela
 * @param seriesExecutadas séries associadas aos itens do treino
 */
public record RegistroHistoricoDTO(
    /** Identifica o treino cuja sessão será persistida e concluída. */
    Long treinoId,
    /** Tempo decorrido desde o início ou retomada da sessão. */
    long segundosDecorridos,
    /** Execuções coletadas na interface e enviadas em uma lista plana. */
    List<SerieExecutadaDTO> seriesExecutadas
) {}
