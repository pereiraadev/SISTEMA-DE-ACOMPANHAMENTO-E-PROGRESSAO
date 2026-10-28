package br.com.sistema.acompanhamento.treino;

import java.time.LocalDateTime;

// Resumo de um treino concluido, devolvido pelo endpoint de historico.
public record HistoricoTreinoDTO(
        Long id,
        String identificacao,
        LocalDateTime dataRealizacao,
        int totalSeries,
        double maiorCarga,
        int maiorRepeticoes,
        double volumeTotal) {
}
