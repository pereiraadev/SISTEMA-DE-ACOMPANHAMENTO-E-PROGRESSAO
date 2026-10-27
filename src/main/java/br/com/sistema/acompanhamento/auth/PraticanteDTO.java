package br.com.sistema.acompanhamento.auth;

// dados que voltam depois do cadastro (nunca a senha)
public record PraticanteDTO(Long id, String nome, String email) {
}
