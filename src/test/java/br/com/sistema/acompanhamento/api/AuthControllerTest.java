package br.com.sistema.acompanhamento.api;

import br.com.sistema.acompanhamento.praticante.domain.*;
import br.com.sistema.acompanhamento.praticante.repository.PraticanteRepository;
import br.com.sistema.acompanhamento.praticante.service.PraticanteService;
import br.com.sistema.acompanhamento.security.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {
    @Test void cadastroNaoDevolveSenhaELoginEmiteToken() {
        PraticanteRepository repo = mock(PraticanteRepository.class);
        var encoder = new BCryptPasswordEncoder();
        var tokens = new TokenService("segredo-de-teste-com-pelo-menos-32-bytes", 3600000);
        var controller = new AuthController(new PraticanteService(repo, encoder), repo, encoder, tokens);
        when(repo.save(any())).thenAnswer(i -> {
            Praticante p = i.getArgument(0);
            ReflectionTestUtils.setField(p, "id", 1L);
            return p;
        });
        ApiDtos.PraticanteResposta cadastro = controller.cadastrar(
                new ApiDtos.Cadastro("Pessoa", "pessoa@example.com", "senha-secreta")).getBody();
        assertEquals("pessoa@example.com", cadastro.email());
        assertEquals(3, ApiDtos.PraticanteResposta.class.getRecordComponents().length);
        Praticante salvo = new Praticante();
        salvo.setNome("Pessoa"); salvo.setEmail("pessoa@example.com");
        salvo.setSenha(encoder.encode("senha-secreta"));
        ReflectionTestUtils.setField(salvo, "id", 1L);
        when(repo.findByEmailIgnoreCase("pessoa@example.com")).thenReturn(Optional.of(salvo));
        ApiDtos.LoginResposta login = controller.login(new ApiDtos.Login("pessoa@example.com", "senha-secreta"));
        assertTrue(tokens.validarToken(login.token()));
        assertEquals("Bearer", login.tipo());
        assertEquals(1L, login.praticante().id());
        assertEquals(HttpStatus.UNAUTHORIZED, assertThrows(ApiException.class,
                () -> controller.login(new ApiDtos.Login("pessoa@example.com", "errada"))).getStatus());
    }
}
