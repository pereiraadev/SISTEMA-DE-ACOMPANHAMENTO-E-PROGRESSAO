package com.fittrack.fittrack_frontend.service;

public final class AuthModels {
    private AuthModels() { }
    public record Cadastro(String nome, String email, String senha) { }
    public record Login(String email, String senha) { }
    public record PraticanteResposta(Long id, String nome, String email) { }
    public record LoginResposta(String token, String tipo, PraticanteResposta praticante) { }
}
