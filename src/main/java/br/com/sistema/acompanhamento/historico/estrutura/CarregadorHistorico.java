package br.com.sistema.acompanhamento.historico.estrutura;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;

// Responsável por carregar os treinos realizados
public class CarregadorHistorico<T extends TreinoHistorico> {

    // Cria uma lista somente com os treinos reais
    public ListaDuplaTreinos<T> carregar(List<T> treinos) {

        ListaDuplaTreinos<T> historico = new ListaDuplaTreinos<>();

        if (treinos == null) return historico;
        ArrayList<T> realizados = new ArrayList<>();
        IdentityHashMap<T, Boolean> vistos = new IdentityHashMap<>();
        for (T treino : treinos) {
            if (treino != null && !treino.isModelo() && !vistos.containsKey(treino)) {
                realizados.add(treino);
                vistos.put(treino, true);
            }
        }
        realizados.sort(Comparator.comparing(TreinoHistorico::getDataRealizacao));
        for (T treino : realizados) historico.inserirFim(treino);

        return historico;
    }
}
