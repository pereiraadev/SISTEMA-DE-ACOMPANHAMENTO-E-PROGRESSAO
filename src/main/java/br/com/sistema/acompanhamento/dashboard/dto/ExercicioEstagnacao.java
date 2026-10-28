package br.com.sistema.acompanhamento.dashboard.dto;

import br.com.sistema.acompanhamento.comparativo.Variacao;
import java.math.BigDecimal;

// tendencia compara a maior carga da primeira sessao com a da ultima
// estagnado = nas ultimas 3 sessoes nem a carga nem o volume passaram da primeira delas
public record ExercicioEstagnacao(
        Long exercicioId,
        String exercicio,
        int sessoes,
        BigDecimal cargaInicial,
        BigDecimal cargaAtual,
        Variacao tendencia,
        boolean estagnado) {
}
