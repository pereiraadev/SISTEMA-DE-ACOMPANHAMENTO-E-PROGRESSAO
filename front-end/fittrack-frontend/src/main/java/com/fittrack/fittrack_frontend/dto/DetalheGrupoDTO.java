package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/** Detalhe de um grupo muscular: volume por exercício e frequência mensal (tela GroupDetail). */
public record DetalheGrupoDTO(
    String grupoMuscular,
    String tagFoco,
    List<VolumeExercicioDTO> volumePorExercicio,
    List<FrequenciaMesDTO> frequenciaMensal
) {}
