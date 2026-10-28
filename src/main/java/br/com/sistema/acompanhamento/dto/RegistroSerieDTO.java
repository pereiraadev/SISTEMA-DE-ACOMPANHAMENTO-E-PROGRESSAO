package br.com.sistema.acompanhamento.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegistroSerieDTO(
        Long treinoId,
        LocalDate dataTreino,
        Long exercicioId,
        String exercicio,
        int numeroSerie,
        BigDecimal cargaUtilizada,
        int repeticoesRealizadas,
        BigDecimal volumeCalculado) {
}
