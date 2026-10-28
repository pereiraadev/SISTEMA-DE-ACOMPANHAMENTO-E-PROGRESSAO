package br.com.sistema.acompanhamento.api;

import br.com.sistema.acompanhamento.praticante.*;
import br.com.sistema.acompanhamento.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PraticanteService praticantes;
    private final PraticanteRepository repository;
    private final PasswordEncoder encoder;
    private final TokenService tokens;
    public AuthController(PraticanteService praticantes, PraticanteRepository repository,
                          PasswordEncoder encoder, TokenService tokens) {
        this.praticantes = praticantes; this.repository = repository; this.encoder = encoder; this.tokens = tokens;
    }
    @PostMapping("/cadastrar")
    public ResponseEntity<ApiDtos.PraticanteResposta> cadastrar(@Valid @RequestBody ApiDtos.Cadastro entrada) {
        try {
            Praticante p = praticantes.cadastrar(entrada.nome(), entrada.email(), entrada.senha());
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiDtos.PraticanteResposta(p.getId(), p.getNome(), p.getEmail()));
        } catch (IllegalArgumentException erro) {
            throw new ApiException(HttpStatus.CONFLICT, "EMAIL_DUPLICADO", "E-mail já cadastrado");
        }
    }
    @PostMapping("/login")
    public ApiDtos.LoginResposta login(@Valid @RequestBody ApiDtos.Login entrada) {
        Praticante p = repository.findByEmailIgnoreCase(entrada.email().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "Credenciais inválidas"));
        if (!encoder.matches(entrada.senha(), p.getSenha()))
            throw new ApiException(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "Credenciais inválidas");
        return new ApiDtos.LoginResposta(tokens.gerarToken(p.getEmail()), "Bearer",
                new ApiDtos.PraticanteResposta(p.getId(), p.getNome(), p.getEmail()));
    }
}
