package br.com.sistema.acompanhamento.treino;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface TreinoRepository extends JpaRepository<Treino, Long> {
    List<Treino> findByPraticanteIdOrderByIdDesc(Long praticanteId);
    @EntityGraph(attributePaths = {"itens", "itens.exercicio"})
    List<Treino> findDistinctByPraticanteIdAndModeloTrueOrderByIdDesc(Long praticanteId);
    @EntityGraph(attributePaths = {"itens", "itens.exercicio"})
    List<Treino> findDistinctByPraticanteIdAndModeloFalseOrderByIdDesc(Long praticanteId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Treino t where t.id = :id")
    Optional<Treino> findLockedById(@Param("id") Long id);
}
