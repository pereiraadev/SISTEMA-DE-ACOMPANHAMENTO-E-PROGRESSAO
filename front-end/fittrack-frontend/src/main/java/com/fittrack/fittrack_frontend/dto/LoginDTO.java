package com.fittrack.fittrack_frontend.dto;

/**
 * Credenciais submetidas pelo formulário de entrada da aplicação.
 *
 * @param email identificador da conta
 * @param senha senha informada pelo usuário
 */
public record LoginDTO(
    /** E-mail enviado ao endpoint de autenticação. */
    String email,
    /** Senha enviada ao endpoint de autenticação. */
    String senha
) {}
