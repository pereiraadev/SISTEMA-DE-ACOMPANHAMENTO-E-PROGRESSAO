package br.com.sistema.acompanhamento.historico.estrutura;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.HashSet;
import java.util.Set;

// Responsável por carregar os treinos realizados
public class CarregadorHistorico<T extends TreinoHistorico> {

    // Cria uma lista somente com os treinos reais
    public ListaDuplaTreinos<T> carregar(List<T> treinos) {

        ListaDuplaTreinos<T> historico = new ListaDuplaTreinos<>();

        if (treinos == null) return historico;
        ArrayList<T> realizados = new ArrayList<>();
        IdentityHashMap<T, Boolean> vistos = new IdentityHashMap<>();
        Set<Long> ids = new HashSet<>();
        for (T treino : treinos) {
            Long id = treino instanceof TreinoComSeries comSeries ? comSeries.getId() : null;
            if (treino != null && !treino.isModelo() && !vistos.containsKey(treino)
                    && (id == null || !ids.contains(id))) {
                realizados.add(treino);
                vistos.put(treino, true);
                if (id != null) ids.add(id);
            }
        }
        realizados.sort(Comparator.comparing(TreinoHistorico::getDataRealizacao)
                .thenComparing(t -> t instanceof TreinoComSeries s ? s.getId() : null,
                        Comparator.nullsLast(Comparator.naturalOrder())));
        for (T treino : realizados) historico.inserirFim(treino);

        return historico;
    }
}
