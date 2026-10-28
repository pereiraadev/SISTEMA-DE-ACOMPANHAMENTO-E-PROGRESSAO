package br.com.sistema.acompanhamento.dto;

// O front manda: Authorization: <tipo> <token>
public record TokenDTO(String token, String tipo) {
}
