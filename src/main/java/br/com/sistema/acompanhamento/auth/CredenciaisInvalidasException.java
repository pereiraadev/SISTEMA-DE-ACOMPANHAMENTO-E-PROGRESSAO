package br.com.sistema.acompanhamento.auth;

public class CredenciaisInvalidasException extends RuntimeException {

    // mesma mensagem pra email errado e senha errada, assim ninguem descobre quais emails existem
    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}
