package br.com.sistema.acompanhamento.praticante.service;

import br.com.sistema.acompanhamento.praticante.domain.Praticante;
import br.com.sistema.acompanhamento.praticante.repository.PraticanteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PraticanteService {
    private final PraticanteRepository repository;
    private final PasswordEncoder passwordEncoder;

    public PraticanteService(PraticanteRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    // Normaliza o e-mail e codifica a senha com BCrypt antes de salvar no banco.
    public Praticante cadastrar(String nome, String email, String senha) {
        String emailNormalizado = email.trim().toLowerCase();
        if (repository.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }
        Praticante praticante = new Praticante();
        praticante.setNome(nome.trim());
        praticante.setEmail(emailNormalizado);
        praticante.setSenha(passwordEncoder.encode(senha));
        return repository.save(praticante);
    }
}
