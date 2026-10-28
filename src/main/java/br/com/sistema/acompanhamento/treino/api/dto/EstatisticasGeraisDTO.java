package br.com.sistema.acompanhamento.treino.api.dto;

import java.math.BigDecimal;

// Totais gerais de um praticante: treinos concluidos, series executadas e volume (carga x repeticoes).
public record EstatisticasGeraisDTO(long totalTreinos, long totalSeries, BigDecimal volumeTotal) {
}
