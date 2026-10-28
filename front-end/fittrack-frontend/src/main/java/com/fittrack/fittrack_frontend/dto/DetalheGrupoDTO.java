package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/**
 * Dados agregados usados para montar a tela de detalhe de um grupo muscular.
 *
 * @param grupoMuscular grupo detalhado
 * @param tagFoco texto auxiliar que destaca o foco do grupo
 * @param volumePorExercicio distribuição do volume entre os exercícios do grupo
 * @param frequenciaMensal quantidade de treinos por mês para o gráfico de frequência
 */
public record DetalheGrupoDTO(
    /** Grupo muscular que identifica a consulta exibida. */
    String grupoMuscular,
    /** Rótulo de foco exibido junto ao nome do grupo. */
    String tagFoco,
    /** Valores que alimentam o gráfico e a legenda de volume. */
    List<VolumeExercicioDTO> volumePorExercicio,
    /** Valores usados para construir as barras de frequência mensal. */
    List<FrequenciaMesDTO> frequenciaMensal
) {}
