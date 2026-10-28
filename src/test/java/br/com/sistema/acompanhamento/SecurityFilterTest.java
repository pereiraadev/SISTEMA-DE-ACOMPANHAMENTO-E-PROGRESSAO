package br.com.sistema.acompanhamento;

import br.com.sistema.acompanhamento.service.TokenService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityFilterTest {
    private final TokenService tokenService = new TokenService(
            "segredo-de-teste-com-pelo-menos-32-bytes", 3_600_000);
    private final SecurityFilter securityFilter = new SecurityFilter(tokenService);

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarTokenValido() throws Exception {
        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + tokenService.gerarToken("pessoa@example.com"));

        securityFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("pessoa@example.com");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticarQuandoNaoHouverToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        securityFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveIgnorarTokenInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");

        securityFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void naoDeveSubstituirAutenticacaoExistente() throws Exception {
        var existente = new UsernamePasswordAuthenticationToken("existente", null);
        SecurityContextHolder.getContext().setAuthentication(existente);
        when(request.getHeader("Authorization"))
                .thenReturn("Bearer " + tokenService.gerarToken("pessoa@example.com"));

        securityFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isSameAs(existente);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void deveIgnorarEsquemaDiferenteDeBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic credencial");

        securityFilter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
