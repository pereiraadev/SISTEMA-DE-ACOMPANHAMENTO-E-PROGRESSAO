package br.com.sistema.acompanhamento.treino;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreinoRepository extends JpaRepository<Treino, Long> {
    List<Treino> findByPraticanteIdOrderByIdDesc(Long praticanteId);
}
