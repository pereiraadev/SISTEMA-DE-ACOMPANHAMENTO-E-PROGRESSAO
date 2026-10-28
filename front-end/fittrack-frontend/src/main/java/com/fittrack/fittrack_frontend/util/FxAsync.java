package com.fittrack.fittrack_frontend.util;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javafx.concurrent.Task;
import javafx.scene.control.Alert;

/**
 * Executa operações demoradas fora da JavaFX Application Thread e entrega seu resultado ou erro
 * de volta na thread da interface, mantendo a renderização e a resposta a eventos fluidas.
 */
public final class FxAsync {

    /** Evita instanciar uma classe utilitária composta somente por operações estáticas. */
    private FxAsync() {}

    /**
     * Executa o trabalho em uma thread daemon e chama o callback correspondente na thread JavaFX.
     *
     * @param trabalho operação possivelmente bloqueante, como uma chamada HTTP
     * @param aoSucesso callback que atualiza a tela com o valor concluído
     * @param aoErro callback que apresenta ou trata a falha na interface
     * @param <T> tipo do valor produzido pelo trabalho
     */
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

    /**
     * Mostra uma caixa de erro com a mensagem da exceção, quando disponível.
     *
     * @param titulo título da caixa de diálogo
     * @param erro falha que deve ser comunicada ao usuário
     */
    public static void exibirErro(String titulo, Throwable erro) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(erro != null && erro.getMessage() != null ? erro.getMessage() : "Erro inesperado.");
        alert.show();
    }

    /**
     * Mostra uma mensagem informativa sem bloquear permanentemente o fluxo da tela.
     *
     * @param titulo título da caixa de diálogo
     * @param mensagem conteúdo apresentado ao usuário
     */
    public static void exibirAviso(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.show();
    }
}
