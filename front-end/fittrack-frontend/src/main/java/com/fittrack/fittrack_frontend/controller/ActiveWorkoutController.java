package com.fittrack.fittrack_frontend.controller;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ActiveWorkoutController {

    @FXML private Label txtCronometro;
    @FXML private Label txtProgressoSeries;
    @FXML private VBox containerSeries;
    @FXML private Label txtVolumeSerie;
    @FXML private Label txtVolumeExercício;

    @FXML private HBox boxNotificacao;
    @FXML private Label lblNotificacaoTexto;

    private int segundosDecorridos = 754;
    private Timeline timeline;
    
    private int serieAtualIndex = 2;
    private final int totalSeries = 4;

    private TextField currentInputCarga;
    private TextField currentInputReps;

    private static class SerieData {
        double carga;
        int reps;
        boolean concluida;

        SerieData(double carga, int reps, boolean concluida) {
            this.carga = carga;
            this.reps = reps;
            this.concluida = concluida;
        }
    }

    private final List<SerieData> listaSeries = new ArrayList<>();

    @FXML
    public void initialize() {
        iniciarCronometro();

        listaSeries.clear();
        listaSeries.add(new SerieData(80.0, 10, true));
        listaSeries.add(new SerieData(82.5, 8, true));
        listaSeries.add(new SerieData(0.0, 0, false));
        listaSeries.add(new SerieData(0.0, 0, false));

        renderizarSeries();
    }

    private void iniciarCronometro() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundosDecorridos++;
            int minutos = segundosDecorridos / 60;
            int segs = segundosDecorridos % 60;
            txtCronometro.setText(String.format("%02d:%02d", minutos, segs));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void renderizarSeries() {
        containerSeries.getChildren().clear();
        double volumeTotal = 0.0;
        int concluidas = 0;

        for (int i = 0; i < listaSeries.size(); i++) {
            SerieData serie = listaSeries.get(i);
            int numeroSerie = i + 1;

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER_LEFT);
            
            ColumnConstraints colNum = new ColumnConstraints();
            colNum.setPercentWidth(12.0);

            ColumnConstraints colCarga = new ColumnConstraints();
            colCarga.setPercentWidth(43.0);

            ColumnConstraints colReps = new ColumnConstraints();
            colReps.setPercentWidth(25.0);

            ColumnConstraints colLog = new ColumnConstraints();
            colLog.setPercentWidth(20.0);
            colLog.setHalignment(HPos.RIGHT);

            grid.getColumnConstraints().addAll(colNum, colCarga, colReps, colLog);

            Label lblNum = new Label(String.valueOf(numeroSerie));
            lblNum.getStyleClass().add("set-number");

            if (serie.concluida) {
                concluidas++;
                volumeTotal += (serie.carga * serie.reps);

                grid.getStyleClass().add("set-row");
                grid.setPrefHeight(45.0);

                Label lblCarga = new Label(serie.carga + " kg");
                lblCarga.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

                Label lblReps = new Label(String.valueOf(serie.reps));
                lblReps.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

                ImageView imgCheck = new ImageView(new Image(getClass().getResourceAsStream("/icons/check-circle.png")));
                imgCheck.setFitWidth(20.0);
                imgCheck.setFitHeight(20.0);

                grid.add(lblNum, 0, 0);
                grid.add(lblCarga, 1, 0);
                grid.add(lblReps, 2, 0);
                grid.add(imgCheck, 3, 0);

            } else if (i == serieAtualIndex) {
                grid.getStyleClass().add("set-row-active");
                grid.setPrefHeight(48.0);
                lblNum.setStyle("-fx-text-fill: #43D61B; -fx-font-weight: bold;");

                currentInputCarga = new TextField();
                currentInputCarga.setPromptText("Carga");
                currentInputCarga.getStyleClass().add("input-set");
                currentInputCarga.setPrefWidth(95.0);

                currentInputReps = new TextField();
                currentInputReps.setPromptText("Reps");
                currentInputReps.getStyleClass().add("input-set");
                currentInputReps.setPrefWidth(60.0);

                ImageView imgEllipse = new ImageView(new Image(getClass().getResourceAsStream("/icons/Ellipse.png")));
                imgEllipse.setFitWidth(20.0);
                imgEllipse.setFitHeight(20.0);

                grid.add(lblNum, 0, 0);
                grid.add(currentInputCarga, 1, 0);
                grid.add(currentInputReps, 2, 0);
                grid.add(imgEllipse, 3, 0);

            } else {
                grid.getStyleClass().add("set-row");
                grid.setPrefHeight(45.0);
                grid.setStyle("-fx-opacity: 0.5;");

                Label lblCarga = new Label("—");
                lblCarga.setStyle("-fx-text-fill: #888888;");

                Label lblReps = new Label("—");
                lblReps.setStyle("-fx-text-fill: #888888;");

                ImageView imgEllipse = new ImageView(new Image(getClass().getResourceAsStream("/icons/Ellipse.png")));
                imgEllipse.setFitWidth(20.0);
                imgEllipse.setFitHeight(20.0);

                grid.add(lblNum, 0, 0);
                grid.add(lblCarga, 1, 0);
                grid.add(lblReps, 2, 0);
                grid.add(imgEllipse, 3, 0);
            }

            containerSeries.getChildren().add(grid);
        }

        txtProgressoSeries.setText(concluidas + " de " + totalSeries + " séries");
        txtVolumeExercício.setText(String.format("%.0f kg", volumeTotal));
    }

    @FXML
    private void handleConfirmarSerie() {
        if (serieAtualIndex >= totalSeries) {
            exibirNotificacaoNaTela("Todas as séries deste exercício já foram concluídas!");
            return;
        }

        if (currentInputCarga == null || currentInputReps == null) return;

        String cargaStr = currentInputCarga.getText().trim();
        String repsStr = currentInputReps.getText().trim();

        if (cargaStr.isEmpty() || repsStr.isEmpty()) {
            exibirNotificacaoNaTela("Preencha Carga e Repetições antes de confirmar!");
            return;
        }

        try {
            double carga = Double.parseDouble(cargaStr);
            int reps = Integer.parseInt(repsStr);

            SerieData serieAtual = listaSeries.get(serieAtualIndex);
            serieAtual.carga = carga;
            serieAtual.reps = reps;
            serieAtual.concluida = true;

            double volumeSerie = carga * reps;
            txtVolumeSerie.setText(String.format("%.0f kg", volumeSerie));

            exibirNotificacaoNaTela("Série " + (serieAtualIndex + 1) + " confirmada! (" + carga + "kg x " + reps + " reps)");

            serieAtualIndex++;
            renderizarSeries();

            if (serieAtualIndex >= totalSeries) {
                exibirNotificacaoNaTela("Parabéns! Exercício finalizado com sucesso!");
            }

        } catch (NumberFormatException e) {
            exibirNotificacaoNaTela("Valores numéricos inválidos para Carga/Reps.");
        }
    }

    private void exibirNotificacaoNaTela(String mensagem) {
        lblNotificacaoTexto.setText(mensagem);
        boxNotificacao.setManaged(true);
        boxNotificacao.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            boxNotificacao.setVisible(false);
            boxNotificacao.setManaged(false);
        });
        delay.play();
    }

    @FXML
    private void handleVoltar() {
        if (timeline != null) timeline.stop();
        System.out.println("Voltar para Home...");
    }

    @FXML
    private void handleFinalizarTreino() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Finalizar Treino");
        alert.setHeaderText("Deseja concluir o treino agora?");
        alert.setContentText("O seu progresso de hoje será salvo.");

        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                if (timeline != null) timeline.stop();
                exibirNotificacaoNaTela("Treino Finalizado com Sucesso!");
            }
        });
    }
}
