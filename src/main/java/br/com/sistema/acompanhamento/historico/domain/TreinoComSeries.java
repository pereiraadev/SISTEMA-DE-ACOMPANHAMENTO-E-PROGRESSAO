package br.com.sistema.acompanhamento.historico.domain;

import java.util.List;

public interface TreinoComSeries extends TreinoHistorico {
    Long getId();
    List<? extends SerieHistorico> getSeriesExecutadas();
}
