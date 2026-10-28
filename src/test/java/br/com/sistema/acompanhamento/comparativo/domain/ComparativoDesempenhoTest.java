
package br.com.sistema.acompanhamento.comparativo.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

// Testa se a comparacao dos exercicios esta funcionando.
public class ComparativoDesempenhoTest {

    ComparativoDesempenho comparativo = new ComparativoDesempenho();

    // Verifica se o sistema identifica aumento de carga.
    @Test
    void testarAumentoCarga() {
        String resultado = comparativo.comparar(40, 10, 45, 10);

        assertEquals("AUMENTO_CARGA", resultado);
    }

    // Verifica se o sistema identifica reducao de carga.
    @Test
    void testarReducaoCarga() {
        String resultado = comparativo.comparar(40, 10, 35, 10);

        assertEquals("REDUCAO_CARGA", resultado);
    }

    // Verifica se houve aumento de repeticoes.
    @Test
    void testarAumentoRepeticoes() {
        String resultado = comparativo.comparar(40, 10, 40, 12);

        assertEquals("AUMENTO_REPETICOES", resultado);
    }

    // Verifica se houve reducao de repeticoes.
    @Test
    void testarReducaoRepeticoes() {
        String resultado = comparativo.comparar(40, 10, 40, 8);

        assertEquals("REDUCAO_REPETICOES", resultado);
    }

    // Verifica se o desempenho permaneceu igual.
    @Test
    void testarManutencao() {
        String resultado = comparativo.comparar(40, 10, 40, 10);

        assertEquals("MANUTENCAO", resultado);
    }

    // Verifica se encontra a maior carga entre varias series.
    @Test
    void testarMelhorSerie() {

        ArrayList<DadosSerie> series = new ArrayList<>();

        series.add(new DadosSerie(40, 12));
        series.add(new DadosSerie(50, 8));
        series.add(new DadosSerie(50, 10));

        DadosSerie melhor = comparativo.buscarMelhorSerie(series);

        assertEquals(50, melhor.getCarga());
        assertEquals(10, melhor.getRepeticoes());
    }

    // Testa a comparacao entre todas as series de dois treinos.
    @Test
    void testarComparacaoExercicios() {

        ArrayList<DadosSerie> anteriores = new ArrayList<>();
        anteriores.add(new DadosSerie(40, 12));
        anteriores.add(new DadosSerie(50, 8));

        ArrayList<DadosSerie> atuais = new ArrayList<>();
        atuais.add(new DadosSerie(45, 12));
        atuais.add(new DadosSerie(50, 10));

        String resultado = comparativo.compararExercicios(
                anteriores, atuais);

        assertEquals("AUMENTO_REPETICOES", resultado);
    }

    // Testa quando o aluno ainda nao tem series anteriores.
    @Test
    void testarSemHistoricoAnterior() {

        // Lista vazia porque nao existe treino anterior.
        ArrayList<DadosSerie> anteriores = new ArrayList<>();

        // Series realizadas no treino atual.
        ArrayList<DadosSerie> atuais = new ArrayList<>();
        atuais.add(new DadosSerie(40, 10));

        String resultado = comparativo.compararExercicios(
                anteriores, atuais);

        // Sem dados anteriores, nao e possivel comparar.
        assertEquals("SEM_DADOS", resultado);
    }

}
