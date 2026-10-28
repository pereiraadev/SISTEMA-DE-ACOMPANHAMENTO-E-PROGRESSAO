package br.com.sistema.acompanhamento.dashboard;

import br.com.sistema.acompanhamento.comparativo.Variacao;
import br.com.sistema.acompanhamento.dashboard.dto.DashboardResposta;
import br.com.sistema.acompanhamento.dashboard.dto.ExercicioEstagnacao;
import br.com.sistema.acompanhamento.dashboard.dto.ExercicioFrequencia;
import br.com.sistema.acompanhamento.dashboard.dto.GrupoMuscularResumo;
import br.com.sistema.acompanhamento.dashboard.dto.Kpis;
import br.com.sistema.acompanhamento.exercicio.Exercicio;
import br.com.sistema.acompanhamento.historico.estrutura.ListaDuplaTreinos;
import br.com.sistema.acompanhamento.historico.estrutura.Ordenador;
import br.com.sistema.acompanhamento.treino.ItemTreino;
import br.com.sistema.acompanhamento.treino.Treino;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// calcula tudo do dashboard percorrendo a lista de treinos uma vez so
public final class DashboardCalculador {

    private static final int JANELA_ESTAGNACAO = 3;
    private static final int TOP_EXERCICIOS = 5;

    private DashboardCalculador() { }

    // uma sessao = o exercicio dentro de um treino
    private static class Sessao {
        final BigDecimal maiorCarga;
        final BigDecimal volume;

        Sessao(BigDecimal maiorCarga, BigDecimal volume) {
            this.maiorCarga = maiorCarga;
            this.volume = volume;
        }
    }

    private static class AcumuladoExercicio {
        final Long id;
        final String nome;
        final List<Sessao> sessoes = new ArrayList<>(); // em ordem cronologica

        AcumuladoExercicio(Long id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    private static class AcumuladoGrupo {
        final String nome;
        int execucoes;
        int series;
        BigDecimal volume = BigDecimal.ZERO;

        AcumuladoGrupo(String nome) {
            this.nome = nome;
        }
    }

    public static DashboardResposta calcular(ListaDuplaTreinos lista, LocalDate hoje) {
        int totalSeries = 0;
        int treinosNaSemana = 0;
        int treinosNoMes = 0;
        BigDecimal volumeAcumulado = BigDecimal.ZERO;
        LocalDate segundaDestaSemana = hoje.with(DayOfWeek.MONDAY);

        Map<LocalDate, Integer> treinosPorSemana = new LinkedHashMap<>();
        Map<Long, AcumuladoExercicio> exercicios = new LinkedHashMap<>();
        Map<String, AcumuladoGrupo> grupos = new LinkedHashMap<>();

        for (Treino treino : lista) {
            LocalDate data = treino.getDataTreino();
            if (data != null) {
                LocalDate segunda = data.with(DayOfWeek.MONDAY);
                treinosPorSemana.merge(segunda, 1, Integer::sum);
                if (segunda.equals(segundaDestaSemana)) {
                    treinosNaSemana++;
                }
                if (data.getYear() == hoje.getYear() && data.getMonth() == hoje.getMonth()) {
                    treinosNoMes++;
                }
            }

            for (ItemTreino item : treino.getItens()) {
                if (item.getSeries().isEmpty()) {
                    continue; // exercicio sem nenhuma serie feita
                }
                BigDecimal volumeItem = item.calcularVolumeTotal();
                int seriesItem = item.getSeries().size();
                Exercicio exercicio = item.getExercicio();

                totalSeries += seriesItem;
                volumeAcumulado = volumeAcumulado.add(volumeItem);

                AcumuladoExercicio acEx = exercicios.computeIfAbsent(
                        exercicio.getId(), id -> new AcumuladoExercicio(id, exercicio.getNome()));
                acEx.sessoes.add(new Sessao(item.obterMaiorCarga(), volumeItem));

                AcumuladoGrupo acGrupo = grupos.computeIfAbsent(exercicio.getGrupoMuscular(), AcumuladoGrupo::new);
                acGrupo.execucoes++;
                acGrupo.series += seriesItem;
                acGrupo.volume = acGrupo.volume.add(volumeItem);
            }
        }

        int maiorFrequenciaSemanal = treinosPorSemana.isEmpty() ? 0 : Collections.max(treinosPorSemana.values());

        Kpis kpis = new Kpis(lista.tamanho(), exercicios.size(), totalSeries, volumeAcumulado,
                treinosNaSemana, treinosNoMes, maiorFrequenciaSemanal);

        return new DashboardResposta(kpis, montarGrupos(grupos), montarMaisRealizados(exercicios),
                montarEstagnacao(exercicios));
    }

    // grupos ordenados do maior volume pro menor
    private static List<GrupoMuscularResumo> montarGrupos(Map<String, AcumuladoGrupo> grupos) {
        List<GrupoMuscularResumo> resultado = new ArrayList<>();
        for (AcumuladoGrupo g : grupos.values()) {
            resultado.add(new GrupoMuscularResumo(g.nome, g.execucoes, g.series, g.volume));
        }
        return Ordenador.mergeSort(resultado, Comparator.comparing(GrupoMuscularResumo::volume).reversed());
    }

    // os 5 exercicios com mais sessoes
    private static List<ExercicioFrequencia> montarMaisRealizados(Map<Long, AcumuladoExercicio> exercicios) {
        List<ExercicioFrequencia> todos = new ArrayList<>();
        for (AcumuladoExercicio e : exercicios.values()) {
            todos.add(new ExercicioFrequencia(e.id, e.nome, e.sessoes.size()));
        }
        List<ExercicioFrequencia> ordenados = Ordenador.mergeSort(
                todos, Comparator.comparingInt(ExercicioFrequencia::sessoes).reversed());
        return new ArrayList<>(ordenados.subList(0, Math.min(TOP_EXERCICIOS, ordenados.size())));
    }

    // so entra exercicio com pelo menos 3 sessoes, os estagnados vem primeiro
    private static List<ExercicioEstagnacao> montarEstagnacao(Map<Long, AcumuladoExercicio> exercicios) {
        List<ExercicioEstagnacao> resultado = new ArrayList<>();
        for (AcumuladoExercicio e : exercicios.values()) {
            int total = e.sessoes.size();
            if (total < JANELA_ESTAGNACAO) {
                continue;
            }
            Sessao primeira = e.sessoes.get(0);
            Sessao ultima = e.sessoes.get(total - 1);
            resultado.add(new ExercicioEstagnacao(e.id, e.nome, total,
                    primeira.maiorCarga, ultima.maiorCarga,
                    Variacao.de(ultima.maiorCarga, primeira.maiorCarga),
                    estaEstagnado(e.sessoes)));
        }
        return Ordenador.mergeSort(resultado, Comparator.comparing(ExercicioEstagnacao::estagnado).reversed());
    }

    // olha as ultimas 3 sessoes: se nenhuma passou da primeira delas (carga e volume), estagnou
    private static boolean estaEstagnado(List<Sessao> sessoes) {
        List<Sessao> janela = sessoes.subList(sessoes.size() - JANELA_ESTAGNACAO, sessoes.size());
        Sessao base = janela.get(0);
        for (int i = 1; i < janela.size(); i++) {
            Sessao atual = janela.get(i);
            if (atual.maiorCarga.compareTo(base.maiorCarga) > 0 || atual.volume.compareTo(base.volume) > 0) {
                return false;
            }
        }
        return true;
    }
}
