package br.com.sistema.acompanhamento.historico.domain;

// Define o que precisamos consultar em cada treino
public interface TreinoComExercicios {

    // Verifica se o treino possui determinado exercício
    boolean possuiExercicio(Long exercicioId);
}