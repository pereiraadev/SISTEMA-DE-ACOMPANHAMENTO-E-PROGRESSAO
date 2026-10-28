package com.fittrack.fittrack_frontend.service;

import org.springframework.stereotype.Service;

@Service
public class WorkoutSession {
    private WorkoutModels.TreinoResposta treino;
    private final java.util.Map<Long, java.util.List<WorkoutModels.SerieResposta>> seriesPorItem = new java.util.HashMap<>();

    public synchronized void registrarSerie(Long itemId, WorkoutModels.SerieResposta serie) {
        seriesPorItem.computeIfAbsent(itemId, chave -> new java.util.ArrayList<>()).add(serie);
    }

    public synchronized java.util.List<WorkoutModels.SerieResposta> series(Long itemId) {
        return java.util.List.copyOf(seriesPorItem.getOrDefault(itemId, java.util.List.of()));
    }

    public synchronized void definir(WorkoutModels.TreinoResposta treino) {
        this.treino = treino;
    }

    public synchronized WorkoutModels.TreinoResposta obter() {
        return treino;
    }

    public synchronized void limpar() {
        treino = null;
        seriesPorItem.clear();
    }
}
