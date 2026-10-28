package br.com.sistema.acompanhamento.treino.repository;

import br.com.sistema.acompanhamento.treino.domain.SerieExecutada;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SerieExecutadaRepository extends JpaRepository<SerieExecutada, Long> {
    List<SerieExecutada> findByItemTreinoIdOrderByNumeroSerieAsc(Long itemTreinoId);
    boolean existsByItemTreinoIdAndNumeroSerie(Long itemTreinoId, Integer numeroSerie);
    @Query("select s from SerieExecutada s join fetch s.itemTreino i join fetch i.exercicio where i.treino.id in :ids order by i.treino.id, i.ordem, s.numeroSerie")
    List<SerieExecutada> findForTreinos(@Param("ids") List<Long> ids);
}
