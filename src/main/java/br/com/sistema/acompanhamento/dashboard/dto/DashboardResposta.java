package br.com.sistema.acompanhamento.dashboard.dto;

import java.util.List;

public record DashboardResposta(
        Kpis kpis,
        List<GrupoMuscularResumo> gruposMusculares,
        List<ExercicioFrequencia> exerciciosMaisRealizados,
        List<ExercicioEstagnacao> estagnacao) {
}
