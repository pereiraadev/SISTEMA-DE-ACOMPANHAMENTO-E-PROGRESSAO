package com.fittrack.fittrack_frontend.client;

import java.util.function.Supplier;

import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

/**
 * Erro de integração que apresenta uma mensagem compreensível para a interface, preservando a causa.
 * Centralizar a tradução evita que cada tela tenha de interpretar exceções específicas do cliente HTTP.
 */
public class ApiException extends RuntimeException {

    /**
     * Cria uma falha de API mantendo a exceção original para diagnóstico.
     *
     * @param mensagem descrição adequada para exibição ao usuário
     * @param causa erro de transporte ou resposta que originou a falha
     */
    public ApiException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }

    /**
     * Executa uma operação REST e converte falhas do RestClient em mensagens uniformes.
     *
     * @param chamada operação HTTP a ser executada
     * @param <T> tipo de resultado retornado pela operação
     * @return valor devolvido pela chamada HTTP
     * @throws ApiException quando o servidor retorna erro ou a comunicação falha
     */
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
