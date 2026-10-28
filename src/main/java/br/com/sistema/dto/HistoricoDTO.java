package br.com.sistema.acompanhamento.dto;

import java.util.List;

public record HistoricoDTO(
        Long exercicioId,
        String ordenarPor,
        String direcao,
        int total,
        List<RegistroSerieDTO> registros) {
}
