package br.com.sistema.acompanhamento.auth;

import br.com.sistema.acompanhamento.praticante.Praticante;
import br.com.sistema.acompanhamento.praticante.PraticanteRepository;
import br.com.sistema.acompanhamento.praticante.PraticanteService;
import br.com.sistema.acompanhamento.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// as duas rotas estao liberadas no SecurityConfig (permitAll)
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthController {

    private final PraticanteService praticanteService;
    private final PraticanteRepository praticanteRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthController(PraticanteService praticanteService, PraticanteRepository praticanteRepository,
                          PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.praticanteService = praticanteService;
        this.praticanteRepository = praticanteRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    // POST /auth/cadastrar  {"nome": "...", "email": "...", "senha": "..."}
    @PostMapping(value = "/cadastrar", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PraticanteDTO> cadastrar(@Valid @RequestBody CadastroDTO dto) {
        if (praticanteRepository.existsByEmailIgnoreCase(dto.email().trim())) {
            throw new EmailJaCadastradoException();
        }

        // o service ja normaliza o email e criptografa a senha
        Praticante praticante = praticanteService.cadastrar(dto.nome(), dto.email(), dto.senha());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PraticanteDTO(praticante.getId(), praticante.getNome(), praticante.getEmail()));
    }

    // POST /auth/login  {"email": "...", "senha": "..."}
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public TokenDTO login(@Valid @RequestBody LoginDTO dto) {
        String email = dto.email().trim().toLowerCase();

        Praticante praticante = praticanteRepository.findByEmailIgnoreCase(email)
                .orElseThrow(CredenciaisInvalidasException::new);

        // matches compara a senha digitada com o hash salvo no banco
        if (!passwordEncoder.matches(dto.senha(), praticante.getSenha())) {
            throw new CredenciaisInvalidasException();
        }

        return new TokenDTO(tokenService.gerarToken(praticante.getEmail()), "Bearer");
    }
}
