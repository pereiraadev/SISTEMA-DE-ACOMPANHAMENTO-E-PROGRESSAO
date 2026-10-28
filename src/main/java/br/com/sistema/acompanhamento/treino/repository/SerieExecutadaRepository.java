package br.com.sistema.acompanhamento.treino.repository;

import br.com.sistema.acompanhamento.treino.domain.SerieExecutada;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SerieExecutadaRepository extends JpaRepository<SerieExecutada, Long> {
    List<SerieExecutada> findByItemTreinoIdOrderByNumeroSerieAsc(Long itemTreinoId);
}
