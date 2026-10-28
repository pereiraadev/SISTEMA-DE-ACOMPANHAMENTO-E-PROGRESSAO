package br.com.sistema.acompanhamento.dto;

import java.util.List;

public record DashboardDTO(
        KpisDTO kpis,
        List<GrupoMuscularDTO> gruposMusculares,
        List<ExercicioFrequenciaDTO> exerciciosMaisRealizados,
        List<ExercicioEstagnacaoDTO> estagnacao) {
}
