package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.dto.ItemTreinoDTO;
import com.fittrack.fittrack_frontend.dto.SerieExecutadaDTO;
import com.fittrack.fittrack_frontend.dto.TreinoAtivoDTO;
import com.fittrack.fittrack_frontend.util.NavigationManager;

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

    @FXML private Label txtNomeTreino;
    @FXML private Label txtNomeExercicio;
    @FXML private Label txtGrupoMuscular;
    @FXML private Label txtReferenciaAnterior;
    @FXML private Label txtCronometro;
    @FXML private Label txtProgressoSeries;
    @FXML private VBox containerSeries;
    @FXML private Label txtVolumeSerie;
    @FXML private Label txtVolumeExercicio;
    @FXML private Label txtVolumeExercício;

    @FXML private HBox boxNotificacao;
    @FXML private Label lblNotificacaoTexto;

    private Long treinoId;
    private Long itemTreinoId;
    private long segundosDecorridos = 0;
    private Timeline timeline;

    private int totalSeriesPrevistas = 0;
    private int serieAtualIndex = 0;

    private TextField currentInputCarga;
    private TextField currentInputReps;

    private final List<SerieExecutadaDTO> listaSeries = new ArrayList<>();

    @FXML
    public void initialize() {
        if (containerSeries != null) {
            containerSeries.getChildren().clear();
        }
    }

    /**
     * Injeta os dados dinâmicos do treino e do exercício vindos da API/Sessão.
     */
    public void carregarDadosTreino(TreinoAtivoDTO treino, ItemTreinoDTO item) {
        if (treino != null) {
            this.treinoId = treino.treinoId();
            this.segundosDecorridos = treino.segundosDecorridos();
            if (txtNomeTreino != null) {
                txtNomeTreino.setText(treino.identificacao());
            }
        }

        if (item != null) {
            this.itemTreinoId = item.id();
            this.totalSeriesPrevistas = item.seriesPrevistas();

            if (txtNomeExercicio != null) txtNomeExercicio.setText(item.nomeExercicio());
            if (txtGrupoMuscular != null) txtGrupoMuscular.setText(item.grupoMuscular());

            if (txtReferenciaAnterior != null) {
                String ref = item.referenciaTreinoAnterior();
                txtReferenciaAnterior.setText((ref != null && !ref.isBlank()) ? ref : "Sem registro anterior");
            }

            this.listaSeries.clear();
            if (item.seriesRealizadas() != null) {
                this.listaSeries.addAll(item.seriesRealizadas());
            }
        }

        calcularProximaSerieAtiva();
        iniciarCronometro();
        renderizarSeries();
    }

    private void calcularProximaSerieAtiva() {
        this.serieAtualIndex = 0;
        for (int i = 0; i < listaSeries.size(); i++) {
            if (listaSeries.get(i).concluida()) {
                this.serieAtualIndex = i + 1;
            } else {
                break;
            }
        }
    }

    private void iniciarCronometro() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundosDecorridos++;
            long minutos = segundosDecorridos / 60;
            long segs = segundosDecorridos % 60;
            if (txtCronometro != null) {
                txtCronometro.setText(String.format("%02d:%02d", minutos, segs));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void renderizarSeries() {
        if (containerSeries == null) return;
        containerSeries.getChildren().clear();

        double volumeTotalExercicio = 0.0;
        int concluidas = 0;

        int totalLinhas = Math.max(listaSeries.size(), totalSeriesPrevistas);

        for (int i = 0; i < totalLinhas; i++) {
            int numeroSerie = i + 1;
            SerieExecutadaDTO serie = (i < listaSeries.size()) ? listaSeries.get(i) : null;
            boolean isConcluida = (serie != null && serie.concluida());

            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER_LEFT);

            ColumnConstraints colNum = new ColumnConstraints(); colNum.setPercentWidth(12.0);
            ColumnConstraints colCarga = new ColumnConstraints(); colCarga.setPercentWidth(43.0);
            ColumnConstraints colReps = new ColumnConstraints(); colReps.setPercentWidth(25.0);
            ColumnConstraints colLog = new ColumnConstraints(); colLog.setPercentWidth(20.0);
            colLog.setHalignment(HPos.RIGHT);

            grid.getColumnConstraints().addAll(colNum, colCarga, colReps, colLog);

            Label lblNum = new Label(String.valueOf(numeroSerie));
            lblNum.getStyleClass().add("set-number");

            if (isConcluida) {
                concluidas++;
                double volumeSerie = serie.cargaUtilizada() * serie.repeticoesRealizadas();
                volumeTotalExercicio += volumeSerie;

                grid.getStyleClass().add("set-row");
                grid.setPrefHeight(45.0);

                Label lblCarga = new Label(serie.cargaUtilizada() + " kg");
                lblCarga.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

                Label lblReps = new Label(String.valueOf(serie.repeticoesRealizadas()));
                lblReps.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

                ImageView imgCheck = new ImageView(carregarIcone("/icons/check-circle.png"));
                if (imgCheck.getImage() != null) {
                    imgCheck.setFitWidth(20.0);
                    imgCheck.setFitHeight(20.0);
                }

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

                ImageView imgEllipse = new ImageView(carregarIcone("/icons/Ellipse.png"));
                if (imgEllipse.getImage() != null) {
                    imgEllipse.setFitWidth(20.0);
                    imgEllipse.setFitHeight(20.0);
                }

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

                ImageView imgEllipse = new ImageView(carregarIcone("/icons/Ellipse.png"));
                if (imgEllipse.getImage() != null) {
                    imgEllipse.setFitWidth(20.0);
                    imgEllipse.setFitHeight(20.0);
                }

                grid.add(lblNum, 0, 0);
                grid.add(lblCarga, 1, 0);
                grid.add(lblReps, 2, 0);
                grid.add(imgEllipse, 3, 0);
            }

            containerSeries.getChildren().add(grid);
        }

        if (txtProgressoSeries != null) {
            txtProgressoSeries.setText(concluidas + " de " + totalSeriesPrevistas + " séries");
        }

        String volumeTexto = String.format("%.0f kg", volumeTotalExercicio);
        if (txtVolumeExercicio != null) txtVolumeExercicio.setText(volumeTexto);
        if (txtVolumeExercício != null) txtVolumeExercício.setText(volumeTexto);
    }

    @FXML
    private void handleConfirmarSerie() {
        if (serieAtualIndex >= totalSeriesPrevistas) {
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

            SerieExecutadaDTO novaSerie = new SerieExecutadaDTO(
                null,
                serieAtualIndex + 1,
                carga,
                reps,
                null,
                carga * reps,
                true
            );

            if (serieAtualIndex < listaSeries.size()) {
                listaSeries.set(serieAtualIndex, novaSerie);
            } else {
                listaSeries.add(novaSerie);
            }

            // Garante o uso dos atributos para evitar warnings e preparar rotas HTTP
            if (this.treinoId != null && this.itemTreinoId != null) {
                System.out.println("Série preparada para persistência na API. Treino ID: " 
                        + getTreinoId() + " | Item ID: " + getItemTreinoId());
            }

            double volumeSerie = carga * reps;
            if (txtVolumeSerie != null) {
                txtVolumeSerie.setText(String.format("%.0f kg", volumeSerie));
            }

            exibirNotificacaoNaTela("Série " + (serieAtualIndex + 1) + " confirmada! (" + carga + "kg x " + reps + " reps)");

            serieAtualIndex++;
            renderizarSeries();

        } catch (NumberFormatException e) {
            exibirNotificacaoNaTela("Valores numéricos inválidos para Carga/Reps.");
        }
    }

    @FXML
    private void handleVoltar() {
        if (timeline != null) timeline.stop();
        if (containerSeries != null) {
            NavigationManager.navegarPara(containerSeries, "/fxml/dashboard_home_view.fxml");
        }
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

                if (getTreinoId() != null) {
                    System.out.println("Finalizando treino ID: " + getTreinoId());
                }

                NavigationManager.navegarPara(containerSeries, "/fxml/dashboard_home_view.fxml");
            }
        });
    }

    public Long getTreinoId() {
        return treinoId;
    }

    public Long getItemTreinoId() {
        return itemTreinoId;
    }

    private Image carregarIcone(String path) {
        var resource = getClass().getResourceAsStream(path);
        return resource != null ? new Image(resource) : null;
    }

    private void exibirNotificacaoNaTela(String mensagem) {
        if (lblNotificacaoTexto == null || boxNotificacao == null) return;

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
}
