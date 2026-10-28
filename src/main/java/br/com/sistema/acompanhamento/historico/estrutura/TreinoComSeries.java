package br.com.sistema.acompanhamento.historico.estrutura;

import java.util.List;

public interface TreinoComSeries extends TreinoHistorico {
    Long getId();
    List<? extends SerieHistorico> getSeriesExecutadas();
}
