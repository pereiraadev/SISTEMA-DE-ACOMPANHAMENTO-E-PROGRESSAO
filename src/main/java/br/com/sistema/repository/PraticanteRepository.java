package br.com.sistema.acompanhamento.repository;

import br.com.sistema.acompanhamento.Praticante;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PraticanteRepository extends JpaRepository<Praticante, Long> {
    Optional<Praticante> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
