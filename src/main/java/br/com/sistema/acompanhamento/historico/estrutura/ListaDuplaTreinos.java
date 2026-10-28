package br.com.sistema.acompanhamento.historico.estrutura;

import br.com.sistema.acompanhamento.historico.dto.RegistroSerie;
import br.com.sistema.acompanhamento.treino.ItemTreino;
import br.com.sistema.acompanhamento.treino.SerieExecutada;
import br.com.sistema.acompanhamento.treino.Treino;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

// historico dos treinos concluidos, lista duplamente encadeada
// inicio = treino mais antigo, fim = mais recente
// nao tem remover porque o grupo usa exclusao logica
public class ListaDuplaTreinos implements Iterable<Treino> {

    private NoTreino inicio;
    private NoTreino fim;
    private int tamanho;

    // monta a lista a partir de treinos ja ordenados do mais antigo pro mais novo
    public static ListaDuplaTreinos de(List<Treino> treinos) {
        ListaDuplaTreinos lista = new ListaDuplaTreinos();
        for (Treino treino : treinos) {
            lista.inserirFim(treino);
        }
        return lista;
    }

    public int tamanho() { return tamanho; }
    public boolean estaVazia() { return tamanho == 0; }

    // O(1)
    public void inserirFim(Treino treino) {
        if (treino == null) {
            throw new IllegalArgumentException("Treino nulo.");
        }
        if (treino.isModelo() || !"CONCLUIDO".equals(treino.getStatus())) {
            throw new IllegalArgumentException("Só treinos concluídos entram no histórico.");
        }

        NoTreino novo = new NoTreino(treino);

        if (fim == null) {
            inicio = novo;
            fim = novo;
        } else {
            novo.setAnterior(fim);
            fim.setProximo(novo);
            fim = novo;
        }
        tamanho++;
    }

    // devolve null se for o primeiro treino
    public Treino obterTreinoAnterior(Treino atual) {
        NoTreino no = buscarNo(atual);
        if (no == null) {
            throw new NoSuchElementException("Treino não está no histórico.");
        }
        return no.getAnterior() == null ? null : no.getAnterior().getTreino();
    }

    // volta de no em no ate achar um treino que tenha o exercicio
    public Treino obterTreinoAnteriorPorExercicio(Treino atual, Long exercicioId) {
        NoTreino no = buscarNo(atual);
        if (no == null) {
            throw new NoSuchElementException("Treino não está no histórico.");
        }
        return procurarParaTras(no.getAnterior(), exercicioId);
    }

    // ultimo treino concluido que fez o exercicio (serve pra treino que ainda esta em andamento)
    public Treino obterUltimoTreinoPorExercicio(Long exercicioId) {
        return procurarParaTras(fim, exercicioId);
    }

    // o exercicio dentro de um treino, null se o treino nao tem
    public ItemTreino obterItemPorExercicio(Treino treino, Long exercicioId) {
        for (ItemTreino item : treino.getItens()) {
            if (Objects.equals(item.getExercicio().getId(), exercicioId)) {
                return item;
            }
        }
        return null;
    }

    // pega as series do exercicio em todos os treinos, O(n)
    public List<RegistroSerie> historicoPorExercicio(Long exercicioId) {
        List<RegistroSerie> resultado = new ArrayList<>();

        for (Treino treino : this) {
            ItemTreino item = obterItemPorExercicio(treino, exercicioId);
            if (item == null) {
                continue;
            }
            for (SerieExecutada serie : item.getSeries()) {
                resultado.add(new RegistroSerie(
                        treino.getId(),
                        treino.getDataTreino(),
                        item.getExercicio().getId(),
                        item.getExercicio().getNome(),
                        serie.getNumeroSerie(),
                        serie.getCargaUtilizada(),
                        serie.getRepeticoesRealizadas(),
                        serie.calcularVolume()));
            }
        }
        return resultado;
    }

    // despachante: escolhe qual dos 4 metodos de ordenacao usar
    // devolve uma lista nova, a original fica igual
    public List<RegistroSerie> ordenarHistorico(List<RegistroSerie> registros,
                                                CriterioOrdenacao criterio, boolean decrescente) {
        if (registros == null || criterio == null) {
            throw new IllegalArgumentException("Registros e critério são obrigatórios.");
        }
        return switch (criterio) {
            case DATA -> ordenarPorData(registros, decrescente);
            case CARGA -> ordenarPorCarga(registros, decrescente);
            case REPETICOES -> ordenarPorRepeticoes(registros, decrescente);
            case VOLUME -> ordenarPorVolume(registros, decrescente);
        };
    }

    private List<RegistroSerie> ordenarPorData(List<RegistroSerie> registros, boolean decrescente) {
        // nullsLast pra nao dar erro se algum treino estiver sem data
        Comparator<LocalDate> datas = Comparator.nullsLast(Comparator.<LocalDate>naturalOrder());
        return ordenar(registros, Comparator.comparing(RegistroSerie::dataTreino, datas), decrescente);
    }

    private List<RegistroSerie> ordenarPorCarga(List<RegistroSerie> registros, boolean decrescente) {
        return ordenar(registros, Comparator.comparing(RegistroSerie::cargaUtilizada), decrescente);
    }

    private List<RegistroSerie> ordenarPorRepeticoes(List<RegistroSerie> registros, boolean decrescente) {
        return ordenar(registros, Comparator.comparingInt(RegistroSerie::repeticoesRealizadas), decrescente);
    }

    private List<RegistroSerie> ordenarPorVolume(List<RegistroSerie> registros, boolean decrescente) {
        return ordenar(registros, Comparator.comparing(RegistroSerie::volumeCalculado), decrescente);
    }

    private List<RegistroSerie> ordenar(List<RegistroSerie> registros,
                                        Comparator<RegistroSerie> criterio, boolean decrescente) {
        return Ordenador.mergeSort(registros, decrescente ? criterio.reversed() : criterio);
    }

    // anda pelos ponteiros anterior a partir do no informado
    private Treino procurarParaTras(NoTreino ponto, Long exercicioId) {
        for (NoTreino n = ponto; n != null; n = n.getAnterior()) {
            if (obterItemPorExercicio(n.getTreino(), exercicioId) != null) {
                return n.getTreino();
            }
        }
        return null;
    }

    // procura do fim pro inicio, os recentes sao mais consultados
    private NoTreino buscarNo(Treino treino) {
        if (treino == null) {
            return null;
        }
        for (NoTreino n = fim; n != null; n = n.getAnterior()) {
            if (Objects.equals(n.getTreino().getId(), treino.getId())) {
                return n;
            }
        }
        return null;
    }

    @Override
    public Iterator<Treino> iterator() {
        return new Iterator<>() {
            private NoTreino atual = inicio;

            @Override
            public boolean hasNext() {
                return atual != null;
            }

            @Override
            public Treino next() {
                if (atual == null) {
                    throw new NoSuchElementException();
                }
                Treino treino = atual.getTreino();
                atual = atual.getProximo();
                return treino;
            }
        };
    }
}
