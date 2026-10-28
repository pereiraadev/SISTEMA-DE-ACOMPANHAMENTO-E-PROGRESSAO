package br.com.sistema.acompanhamento.historico.estrutura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrdenadorTest {

    @Test
    void ordenaCrescente() {
        List<Integer> resultado = Ordenador.mergeSort(List.of(5, 3, 9, 1, 7), Comparator.naturalOrder());

        assertThat(resultado).containsExactly(1, 3, 5, 7, 9);
    }

    @Test
    void ordenaDecrescente() {
        List<Integer> resultado = Ordenador.mergeSort(
                List.of(5, 3, 9, 1, 7), Comparator.<Integer>naturalOrder().reversed());

        assertThat(resultado).containsExactly(9, 7, 5, 3, 1);
    }

    @Test
    void mantemAOrdemNosEmpates() {
        // compara so pela primeira letra, b1 tem que continuar antes de b2
        List<String> resultado = Ordenador.mergeSort(
                List.of("b1", "a1", "b2", "a2"), Comparator.comparing((String texto) -> texto.charAt(0)));

        assertThat(resultado).containsExactly("a1", "a2", "b1", "b2");
    }

    @Test
    void naoAlteraAListaOriginal() {
        List<Integer> original = List.of(3, 1, 2);

        Ordenador.mergeSort(original, Comparator.naturalOrder());

        assertThat(original).containsExactly(3, 1, 2);
    }

    @Test
    void funcionaComListaVaziaEComUmElemento() {
        assertThat(Ordenador.mergeSort(List.<Integer>of(), Comparator.naturalOrder())).isEmpty();
        assertThat(Ordenador.mergeSort(List.of(4), Comparator.naturalOrder())).containsExactly(4);
    }

    @Test
    void recusaNulos() {
        assertThatThrownBy(() -> Ordenador.mergeSort(null, Comparator.<Integer>naturalOrder()))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Ordenador.mergeSort(List.of(1), null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
