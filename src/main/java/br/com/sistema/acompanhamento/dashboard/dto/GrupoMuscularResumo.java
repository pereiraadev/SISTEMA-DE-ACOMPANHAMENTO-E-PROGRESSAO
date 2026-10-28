package br.com.sistema.acompanhamento.dashboard.dto;

import java.math.BigDecimal;

// execucoes = quantas vezes algum exercicio do grupo apareceu num treino
public record GrupoMuscularResumo(
        String grupoMuscular,
        int execucoes,
        int series,
        BigDecimal volume) {
}
