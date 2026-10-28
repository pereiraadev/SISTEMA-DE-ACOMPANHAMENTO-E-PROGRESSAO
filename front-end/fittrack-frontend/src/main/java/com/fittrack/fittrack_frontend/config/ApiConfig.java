package com.fittrack.fittrack_frontend.config;

import com.fittrack.fittrack_frontend.controller.MainController;
import com.fittrack.fittrack_frontend.dto.AuthResponseDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/** Configura o RestClient usado por todos os clientes HTTP do front-end. */
@Configuration
public class ApiConfig {

    @Bean
    public RestClient fitTrackRestClient(@Value("${fittrack.api.base-url}") String baseUrl) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
                // Anexa o token JWT do usuário logado (quando existir) em todas as requisições.
                .requestInterceptor((request, body, execution) -> {
                    AuthResponseDTO usuario = MainController.getUsuarioLogado();
                    if (usuario != null && usuario.token() != null && !usuario.token().isBlank()) {
                        request.getHeaders().setBearerAuth(usuario.token());
                    }
                    return execution.execute(request, body);
                })
                .build();
    }
}
