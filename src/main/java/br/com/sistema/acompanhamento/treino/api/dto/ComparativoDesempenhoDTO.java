package br.com.sistema.acompanhamento.treino.api.dto;

// Resultado da comparacao de um exercicio entre um treino e o treino anterior que tambem o continha.
// resultado: AUMENTO_CARGA, REDUCAO_CARGA, AUMENTO_REPETICOES, REDUCAO_REPETICOES, MANUTENCAO ou SEM_DADOS.
// Os valores "anterior" e as diferencas ficam null quando nao ha base de comparacao (SEM_DADOS).
public record ComparativoDesempenhoDTO(
        Long exercicioId,
        Long treinoAtualId,
        Long treinoAnteriorId,
        String resultado,
        Double cargaAnterior,
        Integer repeticoesAnteriores,
        Double cargaAtual,
        Integer repeticoesAtuais,
        Double diferencaCarga,
        Integer diferencaRepeticoes) {
}
