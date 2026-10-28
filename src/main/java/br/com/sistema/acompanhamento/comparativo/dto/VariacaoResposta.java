package br.com.sistema.acompanhamento.comparativo.dto;

import br.com.sistema.acompanhamento.comparativo.Variacao;
import java.math.BigDecimal;

public record VariacaoResposta(
        Variacao carga,
        Variacao repeticoes,
        Variacao volume,
        BigDecimal diferencaCarga,
        int diferencaRepeticoes,
        BigDecimal diferencaVolume,
        boolean evoluiuPorRepeticoes) {
}
