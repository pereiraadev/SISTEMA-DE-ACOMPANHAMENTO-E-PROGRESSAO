package com.fittrack.fittrack_frontend.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;

/**
 * Executa chamadas bloqueantes (HTTP) fora da JavaFX Application Thread e devolve o resultado
 * (ou o erro) já na thread da interface, evitando travar a tela.
 */
public final class FxAsync {

    private FxAsync() {}

    public static <T> void executar(Supplier<T> trabalho, Consumer<T> aoSucesso, Consumer<Throwable> aoErro) {
        Task<T> tarefa = new Task<>() {
            @Override
            protected T call() {
                return trabalho.get();
            }
        };
        tarefa.setOnSucceeded(e -> aoSucesso.accept(tarefa.getValue()));
        tarefa.setOnFailed(e -> aoErro.accept(tarefa.getException()));

        Thread thread = new Thread(tarefa, "fittrack-api");
        thread.setDaemon(true);
        thread.start();
    }

    public static void exibirErro(String titulo, Throwable erro) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(erro != null && erro.getMessage() != null ? erro.getMessage() : "Erro inesperado.");
        alert.show();
    }

    public static void exibirAviso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();
    }
}
