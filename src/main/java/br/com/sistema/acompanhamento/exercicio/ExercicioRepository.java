package br.com.sistema.acompanhamento.exercicio;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExercicioRepository extends JpaRepository<Exercicio, Long> {
    @Query("select e from Exercicio e left join e.praticante p where e.ativo = true and (p.id = :id or p is null) order by e.nome")
    List<Exercicio> disponiveis(@Param("id") Long praticanteId);
}
