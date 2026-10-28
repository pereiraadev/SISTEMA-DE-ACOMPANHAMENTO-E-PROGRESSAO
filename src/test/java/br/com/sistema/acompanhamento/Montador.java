package br.com.sistema.acompanhamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// As entidades nao tem setter nem construtor publico, entao aqui tem subclasses.
// So pra montar dados nos testes (elas sobrescrevem os getters)
public final class Montador {

    private Montador() { }

    private static class ExercicioFalso extends Exercicio {
        private final long id;
        private final String nome;
        private final String grupo;

        ExercicioFalso(long id, String nome, String grupo) {
            this.id = id;
            this.nome = nome;
            this.grupo = grupo;
        }

        @Override public Long getId() { return id; }
        @Override public String getNome() { return nome; }
        @Override public String getGrupoMuscular() { return grupo; }
    }

    private static class SerieFalsa extends SerieExecutada {
        private final int numero;
        private final BigDecimal carga;
        private final int repeticoes;
        private final BigDecimal volumeDoBanco;

        SerieFalsa(int numero, BigDecimal carga, int repeticoes, BigDecimal volumeDoBanco) {
            this.numero = numero;
            this.carga = carga;
            this.repeticoes = repeticoes;
            this.volumeDoBanco = volumeDoBanco;
        }

        @Override public int getNumeroSerie() { return numero; }
        @Override public BigDecimal getCargaUtilizada() { return carga; }
        @Override public int getRepeticoesRealizadas() { return repeticoes; }
        @Override public BigDecimal getVolumeCalculado() { return volumeDoBanco; }
    }

    private static class ItemFalso extends ItemTreino {
        private final Exercicio exercicio;
        private final List<SerieExecutada> series;

        ItemFalso(Exercicio exercicio, List<SerieExecutada> series) {
            this.exercicio = exercicio;
            this.series = series;
        }

        @Override public Exercicio getExercicio() { return exercicio; }
        @Override public List<SerieExecutada> getSeries() { return series; }
    }

    private static class TreinoFalso extends Treino {
        private final long id;
        private final LocalDate data;
        private final List<ItemTreino> itens;
        private final boolean modelo;
        private final String status;

        TreinoFalso(long id, LocalDate data, List<ItemTreino> itens, boolean modelo, String status) {
            this.id = id;
            this.data = data;
            this.itens = itens;
            this.modelo = modelo;
            this.status = status;
        }

        @Override public Long getId() { return id; }
        @Override public LocalDate getDataTreino() { return data; }
        @Override public List<ItemTreino> getItens() { return itens; }
        @Override public boolean isModelo() { return modelo; }
        @Override public String getStatus() { return status; }
    }

    public static Exercicio exercicio(long id, String nome, String grupoMuscular) {
        return new ExercicioFalso(id, nome, grupoMuscular);
    }

    // Volume fica null, como numa serie que o banco ainda nao calculou.
    public static SerieExecutada serie(int numero, String carga, int repeticoes) {
        return new SerieFalsa(numero, new BigDecimal(carga), repeticoes, null);
    }

    // Serie que ja veio do banco com o volume calculado.
    public static SerieExecutada serieDoBanco(int numero, String carga, int repeticoes, String volume) {
        return new SerieFalsa(numero, new BigDecimal(carga), repeticoes, new BigDecimal(volume));
    }

    public static ItemTreino item(Exercicio exercicio, SerieExecutada... series) {
        return new ItemFalso(exercicio, List.of(series));
    }

    public static Treino treino(long id, LocalDate data, ItemTreino... itens) {
        return new TreinoFalso(id, data, List.of(itens), false, "CONCLUIDO");
    }

    public static Treino emAndamento(long id, LocalDate data, ItemTreino... itens) {
        return new TreinoFalso(id, data, List.of(itens), false, "EM_ANDAMENTO");
    }

    public static Treino modelo(long id, ItemTreino... itens) {
        return new TreinoFalso(id, null, List.of(itens), true, "CONCLUIDO");
    }
}
