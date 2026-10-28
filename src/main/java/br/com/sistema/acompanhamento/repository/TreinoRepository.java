package br.com.sistema.acompanhamento.repository;

import br.com.sistema.acompanhamento.Treino;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TreinoRepository extends JpaRepository<Treino, Long> {

    // So execucoes concluidas, da mais antiga pra mais nova.
    @Query("select t from Treino t "
         + "where t.praticanteId = :praticanteId and t.modelo = false and t.status = 'CONCLUIDO' "
         + "order by t.dataTreino asc, t.id asc")
    List<Treino> buscarExecucoesConcluidas(@Param("praticanteId") Long praticanteId);

    // Busca o treino ja filtrando pelo dono, assim ninguem enxerga treino dos outros.
    Optional<Treino> findByIdAndPraticanteId(Long id, Long praticanteId);
}
