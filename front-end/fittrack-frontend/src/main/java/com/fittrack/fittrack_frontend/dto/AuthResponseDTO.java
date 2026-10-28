package com.fittrack.fittrack_frontend.dto;

/**
 * Dados retornados pela API após autenticação e mantidos pela sessão do front-end.
 * O token autoriza chamadas protegidas e o identificador relaciona as consultas ao praticante.
 *
 * @param token token JWT usado na autenticação das chamadas REST
 * @param praticanteId identificador do praticante autenticado
 * @param nome nome apresentado nas telas
 * @param email e-mail associado à conta
 */
public record AuthResponseDTO(
    /** Credencial JWT enviada no cabeçalho das chamadas à API. */
    String token,
    /** Chave do praticante usada para consultar seus treinos e métricas. */
    Long praticanteId,
    /** Nome para personalização da interface. */
    String nome,
    /** E-mail da conta autenticada. */
    String email
) {}
