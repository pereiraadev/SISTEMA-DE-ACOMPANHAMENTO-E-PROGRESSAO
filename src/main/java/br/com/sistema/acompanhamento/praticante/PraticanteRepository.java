package br.com.sistema.acompanhamento.praticante;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PraticanteRepository extends JpaRepository<Praticante, Long> {
    Optional<Praticante> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
