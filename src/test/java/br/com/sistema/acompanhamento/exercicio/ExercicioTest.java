package br.com.sistema.acompanhamento.exercicio;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ExercicioTest {
    @Test
    void deveAtualizarRecordesEmTempoConstante() {
        Exercicio exercicio = new Exercicio();

        exercicio.atualizarRecordes(new BigDecimal("50.00"), 10, new BigDecimal("500.00"));
        exercicio.atualizarRecordes(new BigDecimal("40.00"), 12, new BigDecimal("480.00"));

        assertThat(exercicio.getRecordeCarga()).isEqualByComparingTo("50.00");
        assertThat(exercicio.getRecordeRepsParaCarga()).isEqualTo(12);
        assertThat(exercicio.getRecordeVolumeSessao()).isEqualByComparingTo("500.00");
        assertThat(exercicio.getTotalVezesRealizado()).isEqualTo(2);
    }
}
