package br.com.sistema.acompanhamento.historico.estrutura;

// Define as informações necessárias para o histórico
public interface TreinoHistorico extends TreinoComExercicios {

    // Informa se o treino é apenas uma ficha modelo
    boolean isModelo();

    // Data da sessão, usada para ordenar o histórico.
    java.time.LocalDateTime getDataRealizacao();
}
