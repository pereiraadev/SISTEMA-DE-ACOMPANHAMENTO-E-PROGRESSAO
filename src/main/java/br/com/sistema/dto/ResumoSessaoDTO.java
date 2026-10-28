package br.com.sistema.acompanhamento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

// Resumo de um exercicio dentro de uma sessao de treino.
public record ResumoSessaoDTO(
        Long treinoId,
        LocalDate dataTreino,
        BigDecimal maiorCarga,
        int totalRepeticoes,
        int totalSeries,
        BigDecimal volumeTotal) {
}
