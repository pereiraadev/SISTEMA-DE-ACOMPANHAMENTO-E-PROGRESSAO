package br.com.sistema.acompanhamento.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

// por padrao o spring security responde 401 e 403 sem corpo,
// aqui ele responde com json pro front conseguir mostrar a mensagem
@Component
public class RespostaJsonSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    // sem token, token invalido ou expirado
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        escrever(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Não autenticado. Envie o header Authorization: Bearer <token>.");
    }

    // logado mas sem permissao
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        escrever(response, HttpServletResponse.SC_FORBIDDEN, "Acesso negado.");
    }

    private void escrever(HttpServletResponse response, int status, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"erro\":\"" + mensagem + "\"}");
    }
}
