package com.fittrack.fittrack_frontend.dto;

/**
 * Corpo enviado ao endpoint de cadastro, reunindo os dados digitados na tela de autenticação.
 *
 * @param nome nome completo do novo praticante
 * @param email endereço usado para identificar a conta
 * @param senha senha informada para criação da credencial
 */
public record CadastroDTO(
    /** Nome exibido no dashboard depois do cadastro. */
    String nome,
    /** E-mail usado como identificador de login. */
    String email,
    /** Senha enviada à API para criação segura da conta. */
    String senha
) {}
