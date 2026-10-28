package br.com.sistema.acompanhamento.historico.domain;

// Define as informações necessárias para o histórico
public interface TreinoHistorico extends TreinoComExercicios {

    // Informa se o treino é apenas uma ficha modelo
    boolean isModelo();

    // Instante de finalizacao em UTC, sem substitutos, usado para ordenar o historico.
    java.time.LocalDateTime getDataRealizacao();
}
