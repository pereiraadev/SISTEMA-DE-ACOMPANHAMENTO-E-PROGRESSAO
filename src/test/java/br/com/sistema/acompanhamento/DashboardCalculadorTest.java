package br.com.sistema.acompanhamento;

import br.com.sistema.acompanhamento.dto.DashboardDTO;
import static br.com.sistema.acompanhamento.Montador.exercicio;
import static br.com.sistema.acompanhamento.Montador.item;
import static br.com.sistema.acompanhamento.Montador.serie;
import static br.com.sistema.acompanhamento.Montador.treino;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DashboardCalculadorTest {

    private static final Exercicio SUPINO = exercicio(1L, "Supino", "PEITO");
    private static final Exercicio AGACHAMENTO = exercicio(2L, "Agachamento", "PERNAS");

    // Quarta-feira, a semana comeca na segunda 26/10.
    private static final LocalDate HOJE = LocalDate.of(2026, 10, 28);

    private DashboardDTO calcularCom(ListaDuplaTreinos lista) {
        return DashboardCalculador.calcular(lista, HOJE);
    }

    private ListaDuplaTreinos listaComSupinoParado() {
        return ListaDuplaTreinos.de(List.of(
                treino(10L, LocalDate.of(2026, 10, 5),
                        item(SUPINO, serie(1, "80.00", 8), serie(2, "70.00", 10))),   // volume 1340
                treino(11L, LocalDate.of(2026, 10, 12),
                        item(AGACHAMENTO, serie(1, "100.00", 5))),                     // volume 500
                treino(12L, LocalDate.of(2026, 10, 26),
                        item(SUPINO, serie(1, "80.00", 8))),                           // volume 640
                treino(13L, LocalDate.of(2026, 10, 27),
                        item(SUPINO, serie(1, "80.00", 8)))));                         // volume 640
    }

    @Test
    void calculaOsKpis() {
        DashboardDTO resposta = calcularCom(listaComSupinoParado());

        assertThat(resposta.kpis().totalTreinos()).isEqualTo(4);
        assertThat(resposta.kpis().totalExercicios()).isEqualTo(2);
        assertThat(resposta.kpis().totalSeries()).isEqualTo(5);
        assertThat(resposta.kpis().volumeAcumulado()).isEqualByComparingTo("3120.00");
        assertThat(resposta.kpis().treinosNaSemana()).isEqualTo(2);
        assertThat(resposta.kpis().treinosNoMes()).isEqualTo(4);
        assertThat(resposta.kpis().maiorFrequenciaSemanal()).isEqualTo(2);
    }

    @Test
    void agrupaPorGrupoMuscularOrdenadoPorVolume() {
        DashboardDTO resposta = calcularCom(listaComSupinoParado());

        assertThat(resposta.gruposMusculares()).hasSize(2);
        assertThat(resposta.gruposMusculares().get(0).grupoMuscular()).isEqualTo("PEITO");
        assertThat(resposta.gruposMusculares().get(0).execucoes()).isEqualTo(3);
        assertThat(resposta.gruposMusculares().get(0).series()).isEqualTo(4);
        assertThat(resposta.gruposMusculares().get(0).volume()).isEqualByComparingTo("2620.00");
        assertThat(resposta.gruposMusculares().get(1).grupoMuscular()).isEqualTo("PERNAS");
    }

    @Test
    void listaOsExerciciosMaisRealizados() {
        DashboardDTO resposta = calcularCom(listaComSupinoParado());

        assertThat(resposta.exerciciosMaisRealizados().get(0).exercicio()).isEqualTo("Supino");
        assertThat(resposta.exerciciosMaisRealizados().get(0).sessoes()).isEqualTo(3);
        assertThat(resposta.exerciciosMaisRealizados().get(1).sessoes()).isEqualTo(1);
    }

    @Test
    void marcaEstagnacaoQuandoNadaPassouDaPrimeiraSessaoDaJanela() {
        DashboardDTO resposta = calcularCom(listaComSupinoParado());

        // So o supino tem 3 sessoes, o agachamento fica de fora.
        assertThat(resposta.estagnacao()).hasSize(1);
        assertThat(resposta.estagnacao().get(0).exercicio()).isEqualTo("Supino");
        assertThat(resposta.estagnacao().get(0).estagnado()).isTrue();
        assertThat(resposta.estagnacao().get(0).tendencia()).isEqualTo("MANUTENCAO");
    }

    @Test
    void naoMarcaEstagnacaoQuandoACargaSobe() {
        ListaDuplaTreinos lista = ListaDuplaTreinos.de(List.of(
                treino(10L, LocalDate.of(2026, 10, 5), item(SUPINO, serie(1, "70.00", 8))),
                treino(11L, LocalDate.of(2026, 10, 12), item(SUPINO, serie(1, "75.00", 8))),
                treino(12L, LocalDate.of(2026, 10, 19), item(SUPINO, serie(1, "80.00", 8)))));

        DashboardDTO resposta = calcularCom(lista);

        assertThat(resposta.estagnacao().get(0).estagnado()).isFalse();
        assertThat(resposta.estagnacao().get(0).tendencia()).isEqualTo("AUMENTO_CARGA");
        assertThat(resposta.estagnacao().get(0).cargaInicial()).isEqualByComparingTo("70.00");
        assertThat(resposta.estagnacao().get(0).cargaAtual()).isEqualByComparingTo("80.00");
    }

    @Test
    void historicoVazioNaoQuebra() {
        DashboardDTO resposta = calcularCom(new ListaDuplaTreinos());

        assertThat(resposta.kpis().totalTreinos()).isZero();
        assertThat(resposta.kpis().volumeAcumulado()).isEqualByComparingTo("0");
        assertThat(resposta.kpis().maiorFrequenciaSemanal()).isZero();
        assertThat(resposta.gruposMusculares()).isEmpty();
        assertThat(resposta.exerciciosMaisRealizados()).isEmpty();
        assertThat(resposta.estagnacao()).isEmpty();
    }
}
