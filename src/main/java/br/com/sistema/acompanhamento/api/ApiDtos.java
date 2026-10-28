package br.com.sistema.acompanhamento.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public final class ApiDtos {
    private ApiDtos() { }
    public record Cadastro(@NotBlank String nome, @Email @NotBlank String email, @NotBlank String senha) { }
    public record Login(@Email @NotBlank String email, @NotBlank String senha) { }
    public record PraticanteResposta(Long id, String nome, String email) { }
    public record LoginResposta(String token, String tipo, PraticanteResposta praticante) { }
    public record ExercicioResposta(Long id, String nome, String grupoMuscular, String descricao, boolean ativo,
                                   BigDecimal recordeCarga, Integer recordeRepsParaCarga,
                                   BigDecimal recordeVolumeSessao, Integer totalVezesRealizado) { }
    public record NovoExercicio(@NotBlank String nome, @NotBlank String grupoMuscular, String descricao) { }
    public record ItemModelo(@NotNull Long exercicioId, @NotNull @Min(1) Integer ordem,
                             @NotNull @Min(1) Integer seriesPrevistas,
                             @NotNull @Min(1) Integer repsPrevistas,
                             @DecimalMin("0.0") BigDecimal cargaPrevista) { }
    public record NovoModelo(@NotBlank String identificacao, @NotEmpty List<@Valid ItemModelo> itens) { }
    public record SerieEntrada(@NotNull @Min(1) Integer numeroSerie,
                               @NotNull @DecimalMin("0.0") BigDecimal carga,
                               @NotNull @Min(1) Integer repeticoes) { }
    public record SerieResposta(Long id, Integer numeroSerie, BigDecimal carga, Integer repeticoes,
                                BigDecimal volume) { }
    public record ItemResposta(Long id, Long exercicioId, String exercicioNome, String grupoMuscular, Integer ordem,
                               Integer seriesPrevistas, Integer repsPrevistas, BigDecimal cargaPrevista,
                               boolean concluido, List<SerieResposta> series, BigDecimal volume) { }
    public record TreinoResposta(Long id, Long modeloOrigemId, String identificacao, boolean modelo,
                                 String status, OffsetDateTime iniciadoEm, OffsetDateTime finalizadoEm,
                                 List<ItemResposta> itens, BigDecimal volume) { }
    public record ComparacaoResposta(Long sessao1Id, Long sessao2Id, Long exercicioId,
                                     BigDecimal volumeSessao1, BigDecimal volumeSessao2,
                                     BigDecimal diferenca, String desempenho) { }
public record DashboardResposta(int sessoesConcluidas, int totalExercicios, int totalSeries,
                                     BigDecimal volumeTotal, Map<String, BigDecimal> volumePorGrupo) { }
    public record MaiorRepsResposta(Long exercicioId, BigDecimal carga, Integer repeticoes) { }
    public record RecordeResposta(Long exercicioId, BigDecimal maiorCarga, Integer repeticoesNaMaiorCarga,
                                 BigDecimal maiorVolumeSessao, int sessoesRealizadas) { }
    public record VolumeExercicioResposta(Long exercicioId, BigDecimal volumeTotal) { }
    public record Erro(String codigo, String mensagem) { }
}
