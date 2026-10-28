package com.fittrack.fittrack_frontend.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class WorkoutModels {
    private WorkoutModels() { }
    public record ExercicioResposta(Long id, String nome, String grupoMuscular, String descricao, boolean ativo, BigDecimal recordeCarga, Integer recordeRepsParaCarga, BigDecimal recordeVolumeSessao, Integer totalVezesRealizado) { }
    public record RecordeResposta(Long exercicioId, BigDecimal maiorCarga, Integer repeticoesNaMaiorCarga, BigDecimal maiorVolumeSessao, int sessoesRealizadas) { }
    public record ItemModelo(Long exercicioId, Integer ordem, Integer seriesPrevistas, Integer repsPrevistas, BigDecimal cargaPrevista) { }
    public record NovoModelo(String identificacao, List<ItemModelo> itens) { }
    public record SerieEntrada(Integer numeroSerie, BigDecimal carga, Integer repeticoes) { }
    public record SerieResposta(Long id, Integer numeroSerie, BigDecimal carga, Integer repeticoes, BigDecimal volume) { }
    public record ItemResposta(Long id, Long exercicioId, String exercicioNome, Integer ordem, Integer seriesPrevistas, Integer repsPrevistas, BigDecimal cargaPrevista, boolean concluido, List<SerieResposta> series, BigDecimal volume) { }
    public record TreinoResposta(Long id, Long modeloOrigemId, String identificacao, boolean modelo, String status, OffsetDateTime iniciadoEm, OffsetDateTime finalizadoEm, List<ItemResposta> itens, BigDecimal volume) { }
}
