package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
public class ExerciseDetailsController {

    @FXML private Label lblNomeExercício;
    @FXML private Label lblGrupoMuscular;
    @FXML private Label lblStatus;
    @FXML private Label lblFiltroAtual;

    @FXML private Label lblMaiorCarga;
    @FXML private Label lblMaiorCargaDet;
    @FXML private Label lblMaiorReps;
    @FXML private Label lblMaiorRepsDet;

    @FXML private LineChart<String, Number> chartEvolucao;
    @FXML private VBox containerHistorico;

    // Elementos do Bottom Sheet de Filtro
    @FXML private StackPane modalOverlay;
    @FXML private ToggleGroup ordenacaoGroup;
    @FXML private RadioButton optRecentes;
    @FXML private RadioButton optAntigos;
    @FXML private RadioButton optMaiorCarga;
    @FXML private RadioButton optMenorCarga;
    @FXML private RadioButton optMaisReps;
    @FXML private RadioButton optMenosReps;
    @FXML private RadioButton optMaiorVolume;
    @FXML private RadioButton optMenorVolume;

    private static class HistoricoEntry {
        String data;
        double carga;
        int reps;
        double volume;
        String rotuloTag;
        String cssClassTag;

        HistoricoEntry(String data, double carga, int reps, double volume, String rotuloTag, String cssClassTag) {
            this.data = data;
            this.carga = carga;
            this.reps = reps;
            this.volume = volume;
            this.rotuloTag = rotuloTag;
            this.cssClassTag = cssClassTag;
        }
    }

    private final List<HistoricoEntry> listaHistorico = new ArrayList<>();

    @FXML
    public void initialize() {
        carregarDadosIniciais();
        renderizarHistorico();
    }

    private void carregarDadosIniciais() {
        // Popula o gráfico de evolução
        chartEvolucao.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Jan", 68));
        series.getData().add(new XYChart.Data<>("Fev", 76));
        series.getData().add(new XYChart.Data<>("Mar", 73));
        series.getData().add(new XYChart.Data<>("Abr", 85));
        series.getData().add(new XYChart.Data<>("Mai", 83));
        series.getData().add(new XYChart.Data<>("Jun", 98));
        chartEvolucao.getData().add(series);

        // Popula lista base do histórico
        listaHistorico.clear();
        listaHistorico.add(new HistoricoEntry("18 Set 2025", 85.0, 10, 3400.0, "Aumento de carga ↑", "tag-aumento"));
        listaHistorico.add(new HistoricoEntry("11 Set 2025", 80.0, 12, 3840.0, "Evolução por reps ↑", "tag-reps"));
        listaHistorico.add(new HistoricoEntry("04 Set 2025", 80.0, 10, 3200.0, "Manutenção =", "tag-manutencao"));
        listaHistorico.add(new HistoricoEntry("28 Ago 2025", 80.0, 8, 2560.0, "Redução ↓", "tag-reducao"));
    }

    private void renderizarHistorico() {
        containerHistorico.getChildren().clear();

        for (HistoricoEntry entry : listaHistorico) {
            HBox card = new HBox(10.0);
            card.getStyleClass().add("history-card");
            card.setAlignment(Pos.CENTER_LEFT);

            VBox info = new VBox(2.0);
            Label lblData = new Label(entry.data);
            lblData.getStyleClass().add("history-date");

            String desc = String.format("4×%d • %.0f kg • Vol: %,.0f kg", entry.reps, entry.carga, entry.volume);
            Label lblDesc = new Label(desc);
            lblDesc.getStyleClass().add("history-desc");

            info.getChildren().addAll(lblData, lblDesc);
            HBox.setHgrow(info, Priority.ALWAYS);

            Label tag = new Label(entry.rotuloTag);
            tag.getStyleClass().add(entry.cssClassTag);

            card.getChildren().addAll(info, tag);
            containerHistorico.getChildren().add(card);
        }
    }

    @FXML
    private void handleAbrirFiltro() {
        modalOverlay.setManaged(true);
        modalOverlay.setVisible(true);
    }

    @FXML
    private void handleFecharFiltro() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
    }

    @FXML
    private void handleAplicarFiltro() {
        RadioButton selected = (RadioButton) ordenacaoGroup.getSelectedToggle();
        if (selected == null) {
            handleFecharFiltro();
            return;
        }

        if (selected == optRecentes) {
            lblFiltroAtual.setText("Data ⤓");
            carregarDadosIniciais();
        } else if (selected == optAntigos) {
            lblFiltroAtual.setText("Data ⤒");
            Collections.reverse(listaHistorico);
        } else if (selected == optMaiorCarga) {
            lblFiltroAtual.setText("Carga ⤓");
            listaHistorico.sort((a, b) -> Double.compare(b.carga, a.carga));
        } else if (selected == optMenorCarga) {
            lblFiltroAtual.setText("Carga ⤒");
            listaHistorico.sort((a, b) -> Double.compare(a.carga, b.carga));
        } else if (selected == optMaisReps) {
            lblFiltroAtual.setText("Reps ⤓");
            listaHistorico.sort((a, b) -> Integer.compare(b.reps, a.reps));
        } else if (selected == optMenosReps) {
            lblFiltroAtual.setText("Reps ⤒");
            listaHistorico.sort((a, b) -> Integer.compare(a.reps, b.reps));
        } else if (selected == optMaiorVolume) {
            lblFiltroAtual.setText("Volume ⤓");
            listaHistorico.sort((a, b) -> Double.compare(b.volume, a.volume));
        } else if (selected == optMenorVolume) {
            lblFiltroAtual.setText("Volume ⤒");
            listaHistorico.sort((a, b) -> Double.compare(a.volume, b.volume));
        }

        renderizarHistorico();
        handleFecharFiltro();
    }

    @FXML
    private void handleVoltar() {
        System.out.println("Navegando de volta...");
    }
}
