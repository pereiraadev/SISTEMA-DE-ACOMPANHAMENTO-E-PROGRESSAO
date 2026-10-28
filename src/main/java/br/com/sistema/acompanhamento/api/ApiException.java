package br.com.sistema.acompanhamento.api;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String codigo;
    public ApiException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }
    public HttpStatus getStatus() { return status; }
    public String getCodigo() { return codigo; }
}
