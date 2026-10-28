package br.com.sistema.acompanhamento.historico.estrutura;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// merge sort, O(n log n) e estavel (empate mantem a ordem de antes)
public final class Ordenador {

    private Ordenador() { }

    public static <T> List<T> mergeSort(List<T> origem, Comparator<? super T> criterio) {
        if (origem == null || criterio == null) {
            throw new IllegalArgumentException("Lista e critério são obrigatórios.");
        }
        // copia pra nao mexer na lista original
        return ordenar(new ArrayList<>(origem), criterio);
    }

    private static <T> List<T> ordenar(List<T> lista, Comparator<? super T> cmp) {
        if (lista.size() <= 1) {
            return lista;
        }

        int meio = lista.size() / 2;
        List<T> esquerda = ordenar(new ArrayList<>(lista.subList(0, meio)), cmp);
        List<T> direita = ordenar(new ArrayList<>(lista.subList(meio, lista.size())), cmp);

        return juntar(esquerda, direita, cmp);
    }

    private static <T> List<T> juntar(List<T> a, List<T> b, Comparator<? super T> cmp) {
        List<T> resultado = new ArrayList<>(a.size() + b.size());
        int i = 0;
        int j = 0;

        while (i < a.size() && j < b.size()) {
            // <= pra ficar estavel
            if (cmp.compare(a.get(i), b.get(j)) <= 0) {
                resultado.add(a.get(i++));
            } else {
                resultado.add(b.get(j++));
            }
        }
        while (i < a.size()) {
            resultado.add(a.get(i++));
        }
        while (j < b.size()) {
            resultado.add(b.get(j++));
        }
        return resultado;
    }
}
