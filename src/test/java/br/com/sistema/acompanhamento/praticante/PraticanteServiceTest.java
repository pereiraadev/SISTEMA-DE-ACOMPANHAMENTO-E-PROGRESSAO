package br.com.sistema.acompanhamento.praticante;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PraticanteServiceTest {
    @Mock
    private PraticanteRepository repository;

    private PasswordEncoder passwordEncoder;
    private PraticanteService service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new PraticanteService(repository, passwordEncoder);
    }

    @Test
    void deveCadastrarComSenhaBCryptEEmailNormalizado() {
        when(repository.existsByEmailIgnoreCase("pessoa@example.com")).thenReturn(false);
        when(repository.save(any(Praticante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Praticante praticante = service.cadastrar(" Pessoa ", " Pessoa@Example.COM ", "senha-secreta");

        assertThat(praticante.getNome()).isEqualTo("Pessoa");
        assertThat(praticante.getEmail()).isEqualTo("pessoa@example.com");
        assertThat(praticante.getSenha()).isNotEqualTo("senha-secreta");
        assertThat(praticante.getSenha()).startsWith("$2");
        assertThat(passwordEncoder.matches("senha-secreta", praticante.getSenha())).isTrue();
        verify(repository).save(praticante);
    }

    @Test
    void deveRecusarEmailDuplicadoSemSalvar() {
        when(repository.existsByEmailIgnoreCase("pessoa@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar("Pessoa", "PESSOA@example.com", "senha-secreta"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("E-mail já cadastrado");

        verify(repository, never()).save(any(Praticante.class));
    }

    @Test
    void deveGerarHashesDiferentesParaAmesmaSenha() {
        when(repository.existsByEmailIgnoreCase("primeiro@example.com")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("segundo@example.com")).thenReturn(false);
        when(repository.save(any(Praticante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Praticante primeiro = service.cadastrar("Primeiro", "primeiro@example.com", "mesma-senha");
        Praticante segundo = service.cadastrar("Segundo", "segundo@example.com", "mesma-senha");

        assertThat(primeiro.getSenha()).isNotEqualTo(segundo.getSenha());
        assertThat(passwordEncoder.matches("mesma-senha", primeiro.getSenha())).isTrue();
        assertThat(passwordEncoder.matches("mesma-senha", segundo.getSenha())).isTrue();
    }
}
