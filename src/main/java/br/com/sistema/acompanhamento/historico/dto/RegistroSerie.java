package br.com.sistema.acompanhamento.historico.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroSerie(
        Long treinoId,
        LocalDate dataTreino,
        Long exercicioId,
        String exercicio,
        int numeroSerie,
        BigDecimal cargaUtilizada,
        int repeticoesRealizadas,
        BigDecimal volumeCalculado) {
}
