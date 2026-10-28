package br.com.sistema.acompanhamento.comparativo;

import static br.com.sistema.acompanhamento.testes.Montador.exercicio;
import static br.com.sistema.acompanhamento.testes.Montador.item;
import static br.com.sistema.acompanhamento.testes.Montador.serie;
import static br.com.sistema.acompanhamento.testes.Montador.treino;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.sistema.acompanhamento.comparativo.dto.ResumoSessao;
import br.com.sistema.acompanhamento.comparativo.dto.VariacaoResposta;
import br.com.sistema.acompanhamento.exercicio.Exercicio;
import br.com.sistema.acompanhamento.treino.ItemTreino;
import br.com.sistema.acompanhamento.treino.SerieExecutada;
import br.com.sistema.acompanhamento.treino.Treino;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ComparativoCalculadorTest {

    private static final Exercicio SUPINO = exercicio(1L, "Supino", "PEITO");

    private ResumoSessao resumo(long id, LocalDate data, String carga, int reps, int series) {
        ItemTreino item = item(SUPINO, repetir(series, carga, reps));
        Treino treino = treino(id, data, item);
        return ComparativoCalculador.resumir(treino, item);
    }

    // cria varias series iguais (mesma carga e repeticoes)
    private SerieExecutada[] repetir(int quantidade, String carga, int reps) {
        SerieExecutada[] series = new SerieExecutada[quantidade];
        for (int i = 0; i < quantidade; i++) {
            series[i] = serie(i + 1, carga, reps);
        }
        return series;
    }

    @Test
    void resumeUmaSessao() {
        ResumoSessao resumo = resumo(10L, LocalDate.of(2026, 10, 1), "80.00", 8, 3);

        assertThat(resumo.maiorCarga()).isEqualByComparingTo("80.00");
        assertThat(resumo.totalRepeticoes()).isEqualTo(24);
        assertThat(resumo.totalSeries()).isEqualTo(3);
        assertThat(resumo.volumeTotal()).isEqualByComparingTo("1920.00");
    }

    @Test
    void detectaAumentoDeCarga() {
        ResumoSessao anterior = resumo(10L, LocalDate.of(2026, 10, 1), "80.00", 8, 3);
        ResumoSessao atual = resumo(11L, LocalDate.of(2026, 10, 8), "82.50", 8, 3);

        VariacaoResposta variacao = ComparativoCalculador.comparar(atual, anterior);

        assertThat(variacao.carga()).isEqualTo(Variacao.AUMENTO);
        assertThat(variacao.diferencaCarga()).isEqualByComparingTo("2.50");
        assertThat(variacao.volume()).isEqualTo(Variacao.AUMENTO);
        assertThat(variacao.evoluiuPorRepeticoes()).isFalse();
    }

    @Test
    void detectaReducaoDeCarga() {
        ResumoSessao anterior = resumo(10L, LocalDate.of(2026, 10, 1), "80.00", 8, 3);
        ResumoSessao atual = resumo(11L, LocalDate.of(2026, 10, 8), "75.00", 8, 3);

        VariacaoResposta variacao = ComparativoCalculador.comparar(atual, anterior);

        assertThat(variacao.carga()).isEqualTo(Variacao.REDUCAO);
        assertThat(variacao.volume()).isEqualTo(Variacao.REDUCAO);
    }

    @Test
    void mesmaCargaMaisRepeticoesContaComoEvolucao() {
        ResumoSessao anterior = resumo(10L, LocalDate.of(2026, 10, 1), "80.00", 8, 3);
        ResumoSessao atual = resumo(11L, LocalDate.of(2026, 10, 8), "80.00", 10, 3);

        VariacaoResposta variacao = ComparativoCalculador.comparar(atual, anterior);

        assertThat(variacao.carga()).isEqualTo(Variacao.MANUTENCAO);
        assertThat(variacao.repeticoes()).isEqualTo(Variacao.AUMENTO);
        assertThat(variacao.evoluiuPorRepeticoes()).isTrue();
        assertThat(variacao.diferencaRepeticoes()).isEqualTo(6);
    }

    @Test
    void sessoesIguaisSaoManutencao() {
        ResumoSessao anterior = resumo(10L, LocalDate.of(2026, 10, 1), "80.00", 8, 3);
        ResumoSessao atual = resumo(11L, LocalDate.of(2026, 10, 8), "80.00", 8, 3);

        VariacaoResposta variacao = ComparativoCalculador.comparar(atual, anterior);

        assertThat(variacao.carga()).isEqualTo(Variacao.MANUTENCAO);
        assertThat(variacao.repeticoes()).isEqualTo(Variacao.MANUTENCAO);
        assertThat(variacao.volume()).isEqualTo(Variacao.MANUTENCAO);
        assertThat(variacao.evoluiuPorRepeticoes()).isFalse();
    }
}
