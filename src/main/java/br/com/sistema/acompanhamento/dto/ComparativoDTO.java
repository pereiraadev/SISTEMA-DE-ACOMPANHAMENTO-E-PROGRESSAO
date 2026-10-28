package br.com.sistema.acompanhamento.dto;

import java.math.BigDecimal;

// Resultado vem da classe ComparativoDesempenho (AUMENTO_CARGA, REDUCAO_CARGA, AUMENTO_REPETICOES,
// REDUCAO_REPETICOES, MANUTENCAO ou SEM_DADOS).
// Anterior e diferencaVolume ficam null quando nao existe sessao anterior com esse exercicio.
public record ComparativoDTO(
        Long treinoId,
        Long exercicioId,
        String exercicio,
        String resultado,
        ResumoSessaoDTO atual,
        ResumoSessaoDTO anterior,
        BigDecimal diferencaVolume) {
}
