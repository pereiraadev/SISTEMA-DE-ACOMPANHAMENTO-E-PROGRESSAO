package br.com.sistema.acompanhamento.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {
    private final TokenService tokenService = new TokenService(
            "segredo-de-teste-com-pelo-menos-32-bytes",
            3_600_000);

    @Test
    void deveGerarEValidarTokenComEmail() {
        String token = tokenService.gerarToken("pessoa@example.com");

        assertThat(tokenService.validarToken(token)).isTrue();
        assertThat(tokenService.extrairEmail(token)).isEqualTo("pessoa@example.com");
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        String token = tokenService.gerarToken("pessoa@example.com");

        assertThat(tokenService.validarToken(token + "adulterado")).isFalse();
    }

    @Test
    void deveRejeitarTokenMalformado() {
        assertThat(tokenService.validarToken("token-invalido")).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() throws InterruptedException {
        TokenService servico = new TokenService(
                "segredo-de-teste-com-pelo-menos-32-bytes",
                1);
        String token = servico.gerarToken("pessoa@example.com");
        Thread.sleep(10);

        assertThat(servico.validarToken(token)).isFalse();
    }

    @Test
    void deveRejeitarSegredoCurto() {
        assertThatThrownBy(() -> new TokenService("segredo-curto", 3_600_000))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarExpiracaoNaoPositiva() {
        assertThatThrownBy(() -> new TokenService(
                "segredo-de-teste-com-pelo-menos-32-bytes", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarTokenAssinadoComOutraChave() {
        String token = Jwts.builder()
                .subject("pessoa@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(Keys.hmacShaKeyFor(
                        "outra-chave-de-teste-com-pelo-menos-32-bytes".getBytes(StandardCharsets.UTF_8)))
                .compact();

        assertThat(tokenService.validarToken(token)).isFalse();
    }
}
