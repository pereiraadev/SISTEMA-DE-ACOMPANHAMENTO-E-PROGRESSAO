package br.com.sistema.acompanhamento.dashboard.dto;

import java.math.BigDecimal;

public record Kpis(
        int totalTreinos,
        int totalExercicios,
        int totalSeries,
        BigDecimal volumeAcumulado,
        int treinosNaSemana,
        int treinosNoMes,
        int maiorFrequenciaSemanal) {
}
