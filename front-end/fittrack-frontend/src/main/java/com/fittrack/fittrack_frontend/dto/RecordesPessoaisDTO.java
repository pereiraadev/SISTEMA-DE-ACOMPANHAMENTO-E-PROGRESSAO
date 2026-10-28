package com.fittrack.fittrack_frontend.dto;

import java.util.List;

/**
 * Conjunto de recordes agregados que preenche o Hall da Fama do praticante.
 *
 * @param maiorCargaAbsoluta maior peso levantado em um exercício
 * @param exercicioMaiorCarga nome do exercício associado ao maior peso
 * @param maiorVolumeTreino maior volume total registrado em uma sessão
 * @param maiorRepsSobCarga resumo do recorde combinado de carga e repetições
 * @param maiorFrequenciaSemanas maior sequência de semanas com treino
 * @param prsPorExercicio lista dos recordes individuais apresentados nos cartões
 */
public record RecordesPessoaisDTO(
    /** Maior carga individual registrada. */
    Double maiorCargaAbsoluta,
    /** Nome do exercício em que ocorreu a maior carga. */
    String exercicioMaiorCarga,
    /** Maior volume agregado de uma sessão. */
    Double maiorVolumeTreino,
    /** Rótulo combinado, por exemplo, "100 kg × 15". */
    String maiorRepsSobCarga,
    /** Maior sequência registrada de semanas de frequência. */
    Integer maiorFrequenciaSemanas,
    /** Recordes individuais por exercício. */
    List<ExercicioDTO> prsPorExercicio
) {}
