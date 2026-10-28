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
import com.fittrack.fittrack_frontend.service.ApiClient;
import com.fittrack.fittrack_frontend.service.NavigationService;
import com.fittrack.fittrack_frontend.service.WorkoutModels;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ActiveWorkoutController {

    @FXML private Label txtCronometro;
    @FXML private Label txtProgressoSeries;
    @FXML private VBox containerSeries;
    @FXML private Label txtVolumeSerie;
    @FXML private Label txtVolumeExercício;
    @FXML private Label txtNomeTreino;
    @FXML private Label txtTreinoAnterior;
    @FXML private Label txtNomeExercicio;
    @FXML private Label txtGrupoMuscular;

    @FXML private HBox boxNotificacao;
    @FXML private Label lblNotificacaoTexto;

    private int segundosDecorridos = 754;
    private Timeline timeline;
    
    private int serieAtualIndex;

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
    private final ApiClient apiClient;
    private final NavigationService navigationService;
    private Long execucaoId;
    private Long itemId;
    private int itemIndex;
    private final com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession;

    public ActiveWorkoutController(ApiClient apiClient, NavigationService navigationService,
                                   com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession) {
        this.apiClient = apiClient;
        this.navigationService = navigationService;
        this.workoutSession = workoutSession;
    }

    @FXML
    public void initialize() {
        iniciarCronometro();

        WorkoutModels.TreinoResposta treino = workoutSession.obter();
        if (treino != null && treino.id() != null && treino.itens() != null && !treino.itens().isEmpty()) {
            execucaoId = treino.id();
            WorkoutModels.ItemResposta item = treino.itens().get(itemIndex);
            itemId = item.id();
            txtNomeTreino.setText(treino.identificacao() == null ? "Treino" : treino.identificacao());
            txtNomeExercicio.setText(item.exercicioNome() == null ? "Exercício" : item.exercicioNome());
            txtGrupoMuscular.setText("Exercício selecionado");
            txtTreinoAnterior.setText(item.cargaPrevista() == null ? "—" : item.cargaPrevista() + " kg");
            listaSeries.clear();
            int previstas = item.seriesPrevistas() == null ? 4 : item.seriesPrevistas();
            for (int i = 0; i < previstas; i++) listaSeries.add(new SerieData(0.0, 0, false));
        } else {
            listaSeries.clear();
            listaSeries.add(new SerieData(80.0, 10, true));
            listaSeries.add(new SerieData(82.5, 8, true));
            listaSeries.add(new SerieData(0.0, 0, false));
            listaSeries.add(new SerieData(0.0, 0, false));
        }
        if (listaSeries.isEmpty()) listaSeries.add(new SerieData(0.0, 0, false));

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

        txtProgressoSeries.setText(concluidas + " de " + listaSeries.size() + " séries");
        txtVolumeExercício.setText(String.format("%.0f kg", volumeTotal));
    }

    @FXML
    private void handleConfirmarSerie() {
            if (serieAtualIndex >= listaSeries.size()) {
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

            if (execucaoId != null && itemId != null) {
                try {
                    WorkoutModels.SerieResposta resposta = apiClient.post("/api/execucoes/" + execucaoId + "/itens/" + itemId + "/series",
                            new WorkoutModels.SerieEntrada(serieAtualIndex + 1, BigDecimal.valueOf(carga), reps),
                            WorkoutModels.SerieResposta.class);
                    workoutSession.registrarSerie(itemId, resposta);
                } catch (Exception erro) {
                    exibirNotificacaoNaTela("Não foi possível salvar a série.");
                    return;
                }
            }

            SerieData serieAtual = listaSeries.get(serieAtualIndex);
            serieAtual.carga = carga;
            serieAtual.reps = reps;
            serieAtual.concluida = true;

            double volumeSerie = carga * reps;
            txtVolumeSerie.setText(String.format("%.0f kg", volumeSerie));

            exibirNotificacaoNaTela("Série " + (serieAtualIndex + 1) + " confirmada! (" + carga + "kg x " + reps + " reps)");

            serieAtualIndex++;
            renderizarSeries();

        if (serieAtualIndex >= listaSeries.size()) {
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
    private void handleExercicioAnterior() {
        if (itemIndex <= 0) return;
        itemIndex--;
        carregarItemAtual();
    }

    @FXML
    private void handleProximoExercicio() {
        WorkoutModels.TreinoResposta treino = workoutSession.obter();
        if (treino == null || treino.itens() == null || itemIndex + 1 >= treino.itens().size()) {
            exibirNotificacaoNaTela("Este é o último exercício.");
            return;
        }
        itemIndex++;
        carregarItemAtual();
    }

    private void carregarItemAtual() {
        WorkoutModels.TreinoResposta treino = workoutSession.obter();
        if (treino == null || treino.itens() == null || treino.itens().isEmpty()) return;
        WorkoutModels.ItemResposta item = treino.itens().get(itemIndex);
        itemId = item.id();
        txtNomeExercicio.setText(item.exercicioNome() == null ? "Exercício" : item.exercicioNome());
        txtTreinoAnterior.setText(item.cargaPrevista() == null ? "—" : item.cargaPrevista() + " kg");
        listaSeries.clear();
        int previstas = item.seriesPrevistas() == null ? 1 : item.seriesPrevistas();
        java.util.List<WorkoutModels.SerieResposta> salvas = workoutSession.series(itemId);
        for (int i = 0; i < previstas; i++) {
            if (i < salvas.size()) {
                WorkoutModels.SerieResposta serie = salvas.get(i);
                listaSeries.add(new SerieData(serie.carga().doubleValue(), serie.repeticoes(), true));
            } else listaSeries.add(new SerieData(0, 0, false));
        }
        serieAtualIndex = salvas.size();
        serieAtualIndex = 0;
        renderizarSeries();
    }

    @FXML
    private void handleVoltar() {
        if (timeline != null) timeline.stop();
        navigationService.navegar("/fxml/dashboard_home_view.fxml");
    }

    @FXML
    private void handleFinalizarTreino() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Finalizar Treino");
        alert.setHeaderText("Deseja concluir o treino agora?");
        alert.setContentText("O seu progresso de hoje será salvo.");

        alert.showAndWait().ifPresent(resposta -> {
            if (resposta == ButtonType.OK) {
                try {
                    if (execucaoId != null) apiClient.post("/api/execucoes/" + execucaoId + "/finalizar", null, WorkoutModels.TreinoResposta.class);
                    if (timeline != null) timeline.stop();
                    exibirNotificacaoNaTela("Treino Finalizado com Sucesso!");
                } catch (Exception erro) {
                    exibirNotificacaoNaTela("Não foi possível salvar o treino.");
                }
            }
        });
    }
}
