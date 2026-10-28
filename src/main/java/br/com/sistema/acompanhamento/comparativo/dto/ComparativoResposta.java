package br.com.sistema.acompanhamento.comparativo.dto;

// anterior e variacao ficam null quando nao existe sessao anterior com esse exercicio
public record ComparativoResposta(
        Long treinoId,
        Long exercicioId,
        String exercicio,
        ResumoSessao atual,
        ResumoSessao anterior,
        VariacaoResposta variacao) {
}
