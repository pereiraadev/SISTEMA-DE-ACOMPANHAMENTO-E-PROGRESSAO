package br.com.sistema.acompanhamento.historico.estrutura;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class HistoricoConsultasTest {
    record Serie(Long getExercicioId, GrupoMuscularEnum getGrupoMuscular,
                 double getCarga, int getRepeticoes) implements SerieHistorico { }

    static class Treino implements TreinoComSeries {
        private final Long id;
        private final LocalDateTime data;
        private final boolean modelo;
        private final List<Serie> series;

        Treino(long id, int dia, boolean modelo, Serie... series) {
            this.id = id;
            this.data = LocalDateTime.of(2026, 1, dia, 10, 0);
            this.modelo = modelo;
            this.series = List.of(series);
        }
        public Long getId() { return id; }
        public LocalDateTime getDataRealizacao() { return data; }
        public boolean isModelo() { return modelo; }
        public List<Serie> getSeriesExecutadas() { return series; }
        public boolean possuiExercicio(Long id) {
            for (Serie serie : series) if (serie.getExercicioId().equals(id)) return true;
            return false;
        }
    }

    private Serie serie(long id, GrupoMuscularEnum grupo, double carga, int reps) {
        return new Serie(id, grupo, carga, reps);
    }

    @Test void carregaVazioESomenteModelos() {
        CarregadorHistorico<Treino> carregador = new CarregadorHistorico<>();
        assertEquals(0, carregador.carregar(List.of()).getTamanho());
        assertEquals(0, carregador.carregar(List.of(new Treino(1, 1, true))).getTamanho());
    }

    @Test void ordenaRealizadosSemDuplicarInstancia() {
        Treino primeiro = new Treino(1, 1, false);
        Treino segundo = new Treino(2, 2, false);
        Treino modelo = new Treino(3, 3, true);
        ListaDuplaTreinos<Treino> lista = new CarregadorHistorico<Treino>()
                .carregar(List.of(segundo, modelo, primeiro, primeiro));
        assertEquals(2, lista.getTamanho());
        assertSame(primeiro, lista.getInicio().getTreino());
        assertSame(segundo, lista.getFim().getTreino());
        assertSame(lista.getFim(), lista.getInicio().getProximo());
        assertSame(lista.getInicio(), lista.getFim().getAnterior());
    }

    @Test void encontraRepeticoesApenasNaCargaEExercicioPedidos() {
        Treino a = new Treino(1, 1, false, serie(1, GrupoMuscularEnum.PEITO, 40, 8),
                serie(1, GrupoMuscularEnum.PEITO, 40, 10), serie(2, GrupoMuscularEnum.COSTAS, 40, 20));
        Treino b = new Treino(2, 2, false, serie(1, GrupoMuscularEnum.PEITO, 50, 6),
                serie(1, GrupoMuscularEnum.PEITO, 40, 12));
        ListaDuplaTreinos<Treino> lista = new CarregadorHistorico<Treino>().carregar(List.of(a, b));
        assertEquals(12, lista.obterMaiorRepsParaCarga(1L, 40));
        assertNull(lista.obterMaiorRepsParaCarga(1L, 60));
        assertNull(new ListaDuplaTreinos<Treino>().obterMaiorRepsParaCarga(1L, 40));
    }

    @Test void comparaVolumesETrataAusencias() {
        Treino a = new Treino(1, 1, false, serie(1, GrupoMuscularEnum.PEITO, 40, 10),
                serie(1, GrupoMuscularEnum.PEITO, 40, 8), serie(2, GrupoMuscularEnum.COSTAS, 100, 5));
        Treino b = new Treino(2, 2, false, serie(1, GrupoMuscularEnum.PEITO, 45, 10),
                serie(1, GrupoMuscularEnum.PEITO, 45, 8));
        ListaDuplaTreinos<Treino> lista = new CarregadorHistorico<Treino>().carregar(List.of(a, b));
        assertEquals(new ComparacaoVolume(720, 810, 90), lista.compararVolumeEntreSessoes(1L, 2L, 1L));
        assertNull(lista.compararVolumeEntreSessoes(1L, 3L, 1L));
        assertNull(lista.compararVolumeEntreSessoes(1L, 2L, 2L));
    }

    @Test void somaVolumePorGrupoSemContarExercicioDeOutroGrupo() {
        Treino a = new Treino(1, 1, false, serie(1, GrupoMuscularEnum.PEITO, 40, 10),
                serie(2, GrupoMuscularEnum.PEITO, 20, 20), serie(3, GrupoMuscularEnum.COSTAS, 30, 10));
        Treino b = new Treino(2, 2, false, serie(1, GrupoMuscularEnum.PEITO, 40, 10));
        ListaDuplaTreinos<Treino> lista = new CarregadorHistorico<Treino>().carregar(List.of(a, b));
        assertEquals(1200, lista.obterVolumePorGrupoMuscular().get(GrupoMuscularEnum.PEITO));
        assertEquals(300, lista.obterVolumePorGrupoMuscular().get(GrupoMuscularEnum.COSTAS));
        assertEquals(0, lista.obterVolumePorGrupoMuscular().get(GrupoMuscularEnum.PERNAS));
    }
    @Test void evitaDuplicidadePorIdEDesempataPeloId() {
        Treino idMaior = new Treino(9, 1, false);
        Treino idMenor = new Treino(2, 1, false);
        Treino repetido = new Treino(9, 2, false);
        ListaDuplaTreinos<Treino> lista = new CarregadorHistorico<Treino>()
                .carregar(List.of(idMaior, repetido, idMenor));
        assertEquals(2, lista.getTamanho());
        assertSame(idMenor, lista.getInicio().getTreino());
        assertSame(idMaior, lista.getFim().getTreino());
    }
}
