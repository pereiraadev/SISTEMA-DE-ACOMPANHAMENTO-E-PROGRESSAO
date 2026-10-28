package br.com.sistema.acompanhamento;

public class CredenciaisInvalidasException extends RuntimeException {

    // Mesma mensagem pra email errado e senha errada, assim ninguem descobre quais emails existem.
    public CredenciaisInvalidasException() {
        super("E-mail ou senha inválidos.");
    }
}
