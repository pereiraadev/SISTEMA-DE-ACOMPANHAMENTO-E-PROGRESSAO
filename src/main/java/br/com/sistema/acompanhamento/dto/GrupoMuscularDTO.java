package br.com.sistema.acompanhamento.dto;

import java.math.BigDecimal;

// Execucoes = quantas vezes algum exercicio do grupo apareceu num treino.
public record GrupoMuscularDTO(
        String grupoMuscular,
        int execucoes,
        int series,
        BigDecimal volume) {
}
