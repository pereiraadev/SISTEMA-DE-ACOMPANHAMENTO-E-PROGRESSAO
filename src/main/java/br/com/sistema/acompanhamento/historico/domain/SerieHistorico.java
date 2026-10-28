package br.com.sistema.acompanhamento.historico.domain;

public interface SerieHistorico {
    Long getExercicioId();
    GrupoMuscularEnum getGrupoMuscular();
    double getCarga();
    int getRepeticoes();
    default java.math.BigDecimal getCargaDecimal() { return java.math.BigDecimal.valueOf(getCarga()); }
}
