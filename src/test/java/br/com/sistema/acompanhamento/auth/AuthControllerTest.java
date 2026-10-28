package br.com.sistema.acompanhamento.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.sistema.acompanhamento.praticante.Praticante;
import br.com.sistema.acompanhamento.praticante.PraticanteRepository;
import br.com.sistema.acompanhamento.praticante.PraticanteService;
import br.com.sistema.acompanhamento.security.TokenService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthControllerTest {

    private PraticanteService praticanteService;
    private PraticanteRepository praticanteRepository;
    private PasswordEncoder passwordEncoder;
    private TokenService tokenService;
    private AuthController controller;

    @BeforeEach
    void preparar() {
        praticanteService = mock(PraticanteService.class);
        praticanteRepository = mock(PraticanteRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        tokenService = mock(TokenService.class);
        controller = new AuthController(praticanteService, praticanteRepository, passwordEncoder, tokenService);
    }

    private Praticante praticante(String nome, String email, String senhaCriptografada) {
        Praticante praticante = new Praticante();
        praticante.setNome(nome);
        praticante.setEmail(email);
        praticante.setSenha(senhaCriptografada);
        return praticante;
    }

    @Test
    void cadastraEDevolve201SemSenha() {
        when(praticanteRepository.existsByEmailIgnoreCase("ana@email.com")).thenReturn(false);
        when(praticanteService.cadastrar("Ana", "ana@email.com", "segredo1"))
                .thenReturn(praticante("Ana", "ana@email.com", "hash"));

        ResponseEntity<PraticanteDTO> resposta =
                controller.cadastrar(new CadastroDTO("Ana", "ana@email.com", "segredo1"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(resposta.getBody().nome()).isEqualTo("Ana");
        assertThat(resposta.getBody().email()).isEqualTo("ana@email.com");
    }

    @Test
    void naoCadastraEmailRepetido() {
        when(praticanteRepository.existsByEmailIgnoreCase("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> controller.cadastrar(new CadastroDTO("Ana", "ana@email.com", "segredo1")))
                .isInstanceOf(EmailJaCadastradoException.class);
        verify(praticanteService, never()).cadastrar("Ana", "ana@email.com", "segredo1");
    }

    @Test
    void loginComSenhaCertaDevolveToken() {
        Praticante ana = praticante("Ana", "ana@email.com", "hash");
        when(praticanteRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("segredo1", "hash")).thenReturn(true);
        when(tokenService.gerarToken("ana@email.com")).thenReturn("token-123");

        TokenDTO token = controller.login(new LoginDTO("  ANA@email.com ", "segredo1"));

        assertThat(token.token()).isEqualTo("token-123");
        assertThat(token.tipo()).isEqualTo("Bearer");
    }

    @Test
    void loginComSenhaErradaFalha() {
        Praticante ana = praticante("Ana", "ana@email.com", "hash");
        when(praticanteRepository.findByEmailIgnoreCase("ana@email.com")).thenReturn(Optional.of(ana));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> controller.login(new LoginDTO("ana@email.com", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class);
        verify(tokenService, never()).gerarToken("ana@email.com");
    }

    @Test
    void loginComEmailInexistenteFalhaComAMesmaMensagem() {
        when(praticanteRepository.findByEmailIgnoreCase("x@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.login(new LoginDTO("x@email.com", "qualquer")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos.");
    }
}
