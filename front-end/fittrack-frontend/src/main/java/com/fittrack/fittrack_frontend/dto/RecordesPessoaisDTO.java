package com.fittrack.fittrack_frontend.dto;

import java.util.List;

public record RecordesPessoaisDTO(
    Double maiorCargaAbsoluta,
    String exercicioMaiorCarga,
    Double maiorVolumeTreino,
    String maiorRepsSobCarga, // Ex: "100 kg × 15"
    Integer maiorFrequenciaSemanas,
    List<ExercicioDTO> prsPorExercicio
) {}
