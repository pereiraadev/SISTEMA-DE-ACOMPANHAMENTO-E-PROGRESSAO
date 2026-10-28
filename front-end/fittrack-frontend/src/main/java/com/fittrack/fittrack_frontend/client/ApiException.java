package com.fittrack.fittrack_frontend.client;

import java.util.function.Supplier;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/** Erro de comunicação com a API, com mensagem já pronta para exibir ao usuário. */
public class ApiException extends RuntimeException {

    public ApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    /** Executa a chamada HTTP traduzindo as exceções do RestClient em ApiException. */
    static <T> T chamar(Supplier<T> chamada) {
        try {
            return chamada.get();
        } catch (RestClientResponseException e) {
            throw new ApiException("O servidor respondeu com erro " + e.getStatusCode().value()
                    + (e.getStatusText() != null && !e.getStatusText().isBlank() ? " (" + e.getStatusText() + ")" : "")
                    + ".", e);
        } catch (ResourceAccessException e) {
            throw new ApiException("Não foi possível conectar ao servidor. Verifique se a API está em execução.", e);
        } catch (RestClientException e) {
            throw new ApiException("Falha na comunicação com o servidor: " + e.getMessage(), e);
        }
    }
}
