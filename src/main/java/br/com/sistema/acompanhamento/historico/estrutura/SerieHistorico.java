package br.com.sistema.acompanhamento.historico.estrutura;

public interface SerieHistorico {
    Long getExercicioId();
    GrupoMuscularEnum getGrupoMuscular();
    double getCarga();
    int getRepeticoes();
}
