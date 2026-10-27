package br.com.sistema.acompanhamento.treino;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieExecutadaRepository extends JpaRepository<SerieExecutada, Long> {
    List<SerieExecutada> findByItemTreinoIdOrderByNumeroSerieAsc(Long itemTreinoId);
}
