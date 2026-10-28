package br.com.sistema.acompanhamento.dto;

import java.math.BigDecimal;

public record KpisDTO(
        int totalTreinos,
        int totalExercicios,
        int totalSeries,
        BigDecimal volumeAcumulado,
        int treinosNaSemana,
        int treinosNoMes,
        int maiorFrequenciaSemanal) {
}
