package br.com.sistema.acompanhamento.historico.dto;

import java.util.List;

public record HistoricoResposta(
        Long exercicioId,
        String ordenarPor,
        String direcao,
        int total,
        List<RegistroSerie> registros) {
}
