package br.com.sistema.acompanhamento.auth;

// o front manda: Authorization: <tipo> <token>
public record TokenDTO(String token, String tipo) {
}
