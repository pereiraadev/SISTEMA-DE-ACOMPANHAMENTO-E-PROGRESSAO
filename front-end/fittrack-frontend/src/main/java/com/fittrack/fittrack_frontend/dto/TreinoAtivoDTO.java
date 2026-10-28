package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/**
 * Estado de uma sessão atualmente em andamento, pronto para ser renderizado na tela de execução.
 *
 * @param treinoId identificador da sessão ativa
 * @param identificacao nome da ficha associada à sessão
 * @param segundosDecorridos tempo já transcorrido, usado para retomar o cronômetro
 * @param itens exercícios ordenados que compõem a sessão
 */
public record TreinoAtivoDTO(
    /** Chave usada para concluir e persistir a sessão. */
    Long treinoId,
    /** Identificação mostrada no cabeçalho do treino. */
    String identificacao,
    /** Duração já registrada antes de abrir ou retomar a tela. */
    long segundosDecorridos,
    /** Itens cuja execução e progresso são exibidos na tela ativa. */
    List<ItemTreinoDTO> itens
) {}
