package br.com.sistema.acompanhamento.treino;

import static br.com.sistema.acompanhamento.testes.Montador.exercicio;
import static br.com.sistema.acompanhamento.testes.Montador.item;
import static br.com.sistema.acompanhamento.testes.Montador.serie;
import static br.com.sistema.acompanhamento.testes.Montador.serieDoBanco;
import static br.com.sistema.acompanhamento.testes.Montador.treino;
import static org.assertj.core.api.Assertions.assertThat;

import br.com.sistema.acompanhamento.exercicio.Exercicio;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class VolumeTest {

    private static final Exercicio SUPINO = exercicio(1L, "Supino", "PEITO");
    private static final Exercicio REMADA = exercicio(2L, "Remada", "COSTAS");

    @Test
    void volumeDaSerieECargaVezesRepeticoes() {
        assertThat(serie(1, "80.00", 8).calcularVolume()).isEqualByComparingTo("640.00");
    }

    @Test
    void usaOVolumeDoBancoQuandoExiste() {
        assertThat(serieDoBanco(1, "80.00", 8, "640.00").calcularVolume()).isEqualByComparingTo("640.00");
    }

    @Test
    void volumeDoExercicioESomaDasSeries() {
        // 3 series de 80 x 8 = 1920
        ItemTreino item = item(SUPINO, serie(1, "80.00", 8), serie(2, "80.00", 8), serie(3, "80.00", 8));

        assertThat(item.calcularVolumeTotal()).isEqualByComparingTo("1920.00");
    }

    @Test
    void itemSemSerieTemVolumeZero() {
        assertThat(item(SUPINO).calcularVolumeTotal()).isEqualByComparingTo("0");
    }

    @Test
    void volumeDoTreinoESomaDosExercicios() {
        Treino treino = treino(1L, LocalDate.of(2026, 10, 1),
                item(SUPINO, serie(1, "80.00", 8), serie(2, "70.00", 10)), // 640 + 700
                item(REMADA, serie(1, "50.00", 12)));                       // 600

        assertThat(treino.calcularVolumeTotal()).isEqualByComparingTo("1940.00");
    }

    @Test
    void maiorCargaETotalDeRepeticoes() {
        ItemTreino item = item(SUPINO, serie(1, "80.00", 8), serie(2, "70.00", 10), serie(3, "82.50", 6));

        assertThat(item.obterMaiorCarga()).isEqualByComparingTo("82.50");
        assertThat(item.contarRepeticoes()).isEqualTo(24);
    }
}
