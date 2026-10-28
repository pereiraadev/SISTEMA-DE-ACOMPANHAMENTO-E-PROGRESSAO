package br.com.sistema.acompanhamento.treino.mapper;

import br.com.sistema.acompanhamento.historico.domain.GrupoMuscularEnum;
import br.com.sistema.acompanhamento.treino.domain.Treino;
import br.com.sistema.acompanhamento.treino.domain.ItemTreino;
import br.com.sistema.acompanhamento.treino.domain.SerieExecutada;
import br.com.sistema.acompanhamento.historico.domain.SerieHistorico;
import br.com.sistema.acompanhamento.historico.domain.TreinoComSeries;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Adapta um Treino (entidade) para as interfaces usadas pela ListaDuplaTreinos.
public class HistoricoTreinoAdapter implements TreinoComSeries {
    private final Treino treino;
    private final List<SerieAdapter> series = new ArrayList<>();

    public HistoricoTreinoAdapter(Treino treino) {
        this.treino = treino;
        for (ItemTreino item : treino.getItens()) {
            for (SerieExecutada serie : item.getSeries()) {
                series.add(new SerieAdapter(item, serie));
            }
        }
    }

    public Treino getTreino() { return treino; }

    @Override
    public Long getId() { return treino.getId(); }

    @Override
    public boolean isModelo() { return treino.isModelo(); }

    // O historico so recebe execucoes com finalizacao registrada.
    @Override
    public LocalDateTime getDataRealizacao() {
        return treino.getFinalizadoEm().withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
    }

    @Override
    public boolean possuiExercicio(Long exercicioId) {
        return series.stream().anyMatch(s -> Objects.equals(exercicioId, s.getExercicioId()));
    }

    @Override
    public List<? extends SerieHistorico> getSeriesExecutadas() { return series; }

    private static class SerieAdapter implements SerieHistorico {
        private final ItemTreino item;
        private final SerieExecutada serie;

        SerieAdapter(ItemTreino item, SerieExecutada serie) {
            this.item = item;
            this.serie = serie;
        }

        @Override
        public Long getExercicioId() {
            return item.getExercicio() == null ? null : item.getExercicio().getId();
        }

        // Grupo desconhecido (ou fora do enum provisorio) vira null e e ignorado nas somas por grupo.
        @Override
        public GrupoMuscularEnum getGrupoMuscular() {
            if (item.getExercicio() == null || item.getExercicio().getGrupoMuscular() == null) return null;
            try {
                return GrupoMuscularEnum.valueOf(item.getExercicio().getGrupoMuscular().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        @Override
        public double getCarga() { return serie.getCargaUtilizada().doubleValue(); }

        @Override
        public int getRepeticoes() { return serie.getRepeticoesRealizadas(); }
    }
}
