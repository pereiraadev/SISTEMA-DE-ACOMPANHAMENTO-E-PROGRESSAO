package br.com.sistema.acompanhamento.dto;

import java.math.BigDecimal;

// Tendencia compara a maior carga da primeira sessao com a da ultima
// (AUMENTO_CARGA, REDUCAO_CARGA ou MANUTENCAO, mesmo texto do ComparativoDesempenho).
// Estagnado = nas ultimas 3 sessoes nem a carga nem o volume passaram da primeira delas.
public record ExercicioEstagnacaoDTO(
        Long exercicioId,
        String exercicio,
        int sessoes,
        BigDecimal cargaInicial,
        BigDecimal cargaAtual,
        String tendencia,
        boolean estagnado) {
}
