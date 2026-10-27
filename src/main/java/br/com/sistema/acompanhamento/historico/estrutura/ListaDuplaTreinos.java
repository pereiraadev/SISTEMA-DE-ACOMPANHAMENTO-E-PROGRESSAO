package br.com.sistema.acompanhamento.historico.estrutura;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

// Guarda os treinos em uma lista duplamente encadeada
public class ListaDuplaTreinos<T extends TreinoComExercicios> {

    // Guardam o primeiro e o último nó da lista
    private NoTreino<T> inicio;
    private NoTreino<T> fim;
    private int tamanho;

    // Adiciona um treino no final da lista
    public void inserirFim(T treino) {
        NoTreino<T> novo = new NoTreino<>(treino);

        // Se a lista estiver vazia, o novo nó será o primeiro
        if (inicio == null) {
            inicio = novo;
            fim = novo;
        } else {
            // Liga o último nó ao novo nó
            fim.setProximo(novo);
            novo.setAnterior(fim);
            fim = novo;
        }

        tamanho++;
    }

    // Retorna a quantidade de treinos na lista
    public int getTamanho() {
        return tamanho;
    }

    // Retorna o primeiro nó da lista
    public NoTreino<T> getInicio() {
        return inicio;
    }

    // Retorna o último nó da lista
    public NoTreino<T> getFim() {
        return fim;
    }

    // Busca o treino anterior que tenha o exercício informado
    public T obterTreinoAnteriorPorExercicio(T treinoAtual, Long exercicioId) {

        // Começa procurando o treino atual na lista
        NoTreino<T> atual = fim;

        while (atual != null) {

            // Encontrou o treino atual, então começa a voltar
            if (atual.getTreino() == treinoAtual) {
                atual = atual.getAnterior();
                break;
            }

            atual = atual.getAnterior();
        }

        // Procura o exercício nos treinos anteriores
        while (atual != null) {

            if (atual.getTreino().possuiExercicio(exercicioId)) {
                return atual.getTreino();
            }

            atual = atual.getAnterior();
        }

        // Não encontrou um treino anterior com esse exercício
        return null;
    }

    // Retorna null quando nenhuma série corresponde à carga e ao exercício.
    public Integer obterMaiorRepsParaCarga(Long exercicioId, double carga) {
        Integer maior = null;
        NoTreino<T> atual = inicio;
        while (atual != null) {
            if (atual.getTreino() instanceof TreinoComSeries treino) {
                for (SerieHistorico serie : treino.getSeriesExecutadas()) {
                    if (Objects.equals(exercicioId, serie.getExercicioId())
                            && Double.compare(carga, serie.getCarga()) == 0
                            && (maior == null || serie.getRepeticoes() > maior)) {
                        maior = serie.getRepeticoes();
                    }
                }
            }
            atual = atual.getProximo();
        }
        return maior;
    }

    // Retorna null se alguma sessão não existir ou não tiver o exercício.
    public ComparacaoVolume compararVolumeEntreSessoes(Long id1, Long id2, Long exercicioId) {
        Double primeiro = null;
        Double segundo = null;
        NoTreino<T> atual = inicio;
        while (atual != null) {
            if (atual.getTreino() instanceof TreinoComSeries treino) {
                if (Objects.equals(id1, treino.getId())) primeiro = volume(treino, exercicioId);
                if (Objects.equals(id2, treino.getId())) segundo = volume(treino, exercicioId);
            }
            atual = atual.getProximo();
        }
        if (primeiro == null || segundo == null) return null;
        return new ComparacaoVolume(primeiro, segundo, segundo - primeiro);
    }

    private Double volume(TreinoComSeries treino, Long exercicioId) {
        double total = 0;
        boolean encontrou = false;
        for (SerieHistorico serie : treino.getSeriesExecutadas()) {
            if (Objects.equals(exercicioId, serie.getExercicioId())) {
                total += serie.getCarga() * serie.getRepeticoes();
                encontrou = true;
            }
        }
        return encontrou ? total : null;
    }

    // Um grupo sem séries realizadas permanece com volume zero.
    public Map<GrupoMuscularEnum, Double> obterVolumePorGrupoMuscular() {
        Map<GrupoMuscularEnum, Double> volumes = new EnumMap<>(GrupoMuscularEnum.class);
        for (GrupoMuscularEnum grupo : GrupoMuscularEnum.values()) volumes.put(grupo, 0.0);
        NoTreino<T> atual = inicio;
        while (atual != null) {
            if (atual.getTreino() instanceof TreinoComSeries treino) {
                for (SerieHistorico serie : treino.getSeriesExecutadas()) {
                    GrupoMuscularEnum grupo = serie.getGrupoMuscular();
                    if (grupo != null) volumes.put(grupo,
                            volumes.get(grupo) + serie.getCarga() * serie.getRepeticoes());
                }
            }
            atual = atual.getProximo();
        }
        return volumes;
    }
}
