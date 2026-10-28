package com.fittrack.fittrack_frontend.service;

import org.springframework.stereotype.Service;

@Service
public class ExerciseSelection {
    private Long exerciseId;

    public synchronized void selecionar(Long exerciseId) {
        this.exerciseId = exerciseId;
    }

    public synchronized Long obter() {
        return exerciseId;
    }
}
