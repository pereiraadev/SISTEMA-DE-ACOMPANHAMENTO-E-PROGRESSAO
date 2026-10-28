package br.com.sistema.acompanhamento;

import br.com.sistema.acompanhamento.dto.RegistroSerieDTO;
import static br.com.sistema.acompanhamento.Montador.emAndamento;
import static br.com.sistema.acompanhamento.Montador.exercicio;
import static br.com.sistema.acompanhamento.Montador.item;
import static br.com.sistema.acompanhamento.Montador.modelo;
import static br.com.sistema.acompanhamento.Montador.serie;
import static br.com.sistema.acompanhamento.Montador.treino;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ListaDuplaTreinosTest {

    private static final Exercicio SUPINO = exercicio(1L, "Supino", "PEITO");
    private static final Exercicio AGACHAMENTO = exercicio(2L, "Agachamento", "PERNAS");

    private ListaDuplaTreinos lista;
    private Treino treino1;
    private Treino treino2;
    private Treino treino3;

    @BeforeEach
    void preparar() {
        // Treino 1: supino 80x8 e 70x10, treino 2: so agachamento, treino 3: supino 82,5x6.
        treino1 = treino(10L, LocalDate.of(2026, 10, 1),
                item(SUPINO, serie(1, "80.00", 8), serie(2, "70.00", 10)));
        treino2 = treino(11L, LocalDate.of(2026, 10, 8),
                item(AGACHAMENTO, serie(1, "100.00", 5)));
        treino3 = treino(12L, LocalDate.of(2026, 10, 15),
                item(SUPINO, serie(1, "82.50", 6)));

        lista = ListaDuplaTreinos.de(List.of(treino1, treino2, treino3));
    }

    @Test
    void guardaOsTreinosNaOrdemDeInsercao() {
        assertThat(lista.tamanho()).isEqualTo(3);
        assertThat(lista).containsExactly(treino1, treino2, treino3);
    }

    @Test
    void achaOTreinoAnterior() {
        assertThat(lista.obterTreinoAnterior(treino3)).isSameAs(treino2);
        assertThat(lista.obterTreinoAnterior(treino1)).isNull();
    }

    @Test
    void achaOTreinoAnteriorQueFezOExercicio() {
        // O treino 2 nao tem supino, entao o anterior do treino 3 e o treino 1.
        assertThat(lista.obterTreinoAnteriorPorExercicio(treino3, 1L)).isSameAs(treino1);
        assertThat(lista.obterTreinoAnteriorPorExercicio(treino1, 1L)).isNull();
    }

    @Test
    void achaOUltimoTreinoQueFezOExercicio() {
        assertThat(lista.obterUltimoTreinoPorExercicio(1L)).isSameAs(treino3);
        assertThat(lista.obterUltimoTreinoPorExercicio(2L)).isSameAs(treino2);
        assertThat(lista.obterUltimoTreinoPorExercicio(99L)).isNull();
    }

    @Test
    void juntaAsSeriesDoExercicioEmOrdemCronologica() {
        List<RegistroSerieDTO> series = lista.historicoPorExercicio(1L);

        assertThat(series).hasSize(3);
        assertThat(series.get(0).dataTreino()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(series.get(2).dataTreino()).isEqualTo(LocalDate.of(2026, 10, 15));
        assertThat(series.get(0).volumeCalculado()).isEqualByComparingTo("640.00");
    }

    @Test
    void ordenarHistoricoPorCarga() {
        List<RegistroSerieDTO> series = lista.historicoPorExercicio(1L);

        assertThat(lista.ordenarHistorico(series, CriterioOrdenacao.CARGA, false))
                .extracting(registro -> registro.cargaUtilizada().doubleValue())
                .containsExactly(70.0, 80.0, 82.5);

        assertThat(lista.ordenarHistorico(series, CriterioOrdenacao.CARGA, true).get(0).cargaUtilizada())
                .isEqualByComparingTo("82.50");
    }

    @Test
    void ordenarHistoricoPorRepeticoesVolumeEData() {
        List<RegistroSerieDTO> series = lista.historicoPorExercicio(1L);

        assertThat(lista.ordenarHistorico(series, CriterioOrdenacao.REPETICOES, true).get(0).repeticoesRealizadas())
                .isEqualTo(10);
        assertThat(lista.ordenarHistorico(series, CriterioOrdenacao.VOLUME, true).get(0).volumeCalculado())
                .isEqualByComparingTo("700.00");
        assertThat(lista.ordenarHistorico(series, CriterioOrdenacao.DATA, true).get(0).dataTreino())
                .isEqualTo(LocalDate.of(2026, 10, 15));
    }

    @Test
    void ordenarHistoricoNaoAlteraAListaOriginal() {
        List<RegistroSerieDTO> series = lista.historicoPorExercicio(1L);
        RegistroSerieDTO primeira = series.get(0);

        lista.ordenarHistorico(series, CriterioOrdenacao.CARGA, true);

        assertThat(series.get(0)).isEqualTo(primeira);
    }

    @Test
    void ordenarHistoricoRecusaNulos() {
        assertThatThrownBy(() -> lista.ordenarHistorico(null, CriterioOrdenacao.DATA, false))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lista.ordenarHistorico(List.of(), null, false))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void naoAceitaModeloNemTreinoEmAndamento() {
        assertThatThrownBy(() -> lista.inserirFim(modelo(20L))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lista.inserirFim(emAndamento(21L, LocalDate.now())))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lista.inserirFim(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void funcionaComListaVazia() {
        ListaDuplaTreinos vazia = new ListaDuplaTreinos();

        assertThat(vazia.estaVazia()).isTrue();
        assertThat(vazia.historicoPorExercicio(1L)).isEmpty();
    }
}
