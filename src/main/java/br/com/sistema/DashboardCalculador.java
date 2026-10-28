package br.com.sistema.acompanhamento;

import br.com.sistema.acompanhamento.dto.DashboardDTO;
import br.com.sistema.acompanhamento.dto.ExercicioEstagnacaoDTO;
import br.com.sistema.acompanhamento.dto.ExercicioFrequenciaDTO;
import br.com.sistema.acompanhamento.dto.GrupoMuscularDTO;
import br.com.sistema.acompanhamento.dto.KpisDTO;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Calcula tudo do dashboard percorrendo a lista de treinos uma vez so.
public final class DashboardCalculador {

    private static final int JANELA_ESTAGNACAO = 3;
    private static final int TOP_EXERCICIOS = 5;

    // Reaproveita a comparacao de carga que ja existia no ComparativoDesempenho.
    private static final ComparativoDesempenho COMPARATIVO = new ComparativoDesempenho();

    private DashboardCalculador() { }

    // Uma sessao = o exercicio dentro de um treino.
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

    public static DashboardDTO calcular(ListaDuplaTreinos lista, LocalDate hoje) {
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

        KpisDTO kpis = new KpisDTO(lista.tamanho(), exercicios.size(), totalSeries, volumeAcumulado,
                treinosNaSemana, treinosNoMes, maiorFrequenciaSemanal);

        return new DashboardDTO(kpis, montarGrupos(grupos), montarMaisRealizados(exercicios),
                montarEstagnacao(exercicios));
    }

    // Grupos ordenados do maior volume pro menor.
    private static List<GrupoMuscularDTO> montarGrupos(Map<String, AcumuladoGrupo> grupos) {
        List<GrupoMuscularDTO> resultado = new ArrayList<>();
        for (AcumuladoGrupo g : grupos.values()) {
            resultado.add(new GrupoMuscularDTO(g.nome, g.execucoes, g.series, g.volume));
        }
        return Ordenador.mergeSort(resultado, Comparator.comparing(GrupoMuscularDTO::volume).reversed());
    }

    // Os 5 exercicios com mais sessoes.
    private static List<ExercicioFrequenciaDTO> montarMaisRealizados(Map<Long, AcumuladoExercicio> exercicios) {
        List<ExercicioFrequenciaDTO> todos = new ArrayList<>();
        for (AcumuladoExercicio e : exercicios.values()) {
            todos.add(new ExercicioFrequenciaDTO(e.id, e.nome, e.sessoes.size()));
        }
        List<ExercicioFrequenciaDTO> ordenados = Ordenador.mergeSort(
                todos, Comparator.comparingInt(ExercicioFrequenciaDTO::sessoes).reversed());
        return new ArrayList<>(ordenados.subList(0, Math.min(TOP_EXERCICIOS, ordenados.size())));
    }

    // So entra exercicio com pelo menos 3 sessoes, os estagnados vem primeiro.
    private static List<ExercicioEstagnacaoDTO> montarEstagnacao(Map<Long, AcumuladoExercicio> exercicios) {
        List<ExercicioEstagnacaoDTO> resultado = new ArrayList<>();
        for (AcumuladoExercicio e : exercicios.values()) {
            int total = e.sessoes.size();
            if (total < JANELA_ESTAGNACAO) {
                continue;
            }
            Sessao primeira = e.sessoes.get(0);
            Sessao ultima = e.sessoes.get(total - 1);
            resultado.add(new ExercicioEstagnacaoDTO(e.id, e.nome, total,
                    primeira.maiorCarga, ultima.maiorCarga,
                    COMPARATIVO.comparar(primeira.maiorCarga.doubleValue(), 0, ultima.maiorCarga.doubleValue(), 0),
                    estaEstagnado(e.sessoes)));
        }
        return Ordenador.mergeSort(resultado, Comparator.comparing(ExercicioEstagnacaoDTO::estagnado).reversed());
    }

    // Olha as ultimas 3 sessoes: se nenhuma passou da primeira delas (carga e volume), estagnou.
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
