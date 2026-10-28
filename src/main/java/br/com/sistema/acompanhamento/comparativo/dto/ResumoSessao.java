package br.com.sistema.acompanhamento.comparativo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// resumo de um exercicio dentro de uma sessao de treino
public record ResumoSessao(
        Long treinoId,
        LocalDate dataTreino,
        BigDecimal maiorCarga,
        int totalRepeticoes,
        int totalSeries,
        BigDecimal volumeTotal) {
}
