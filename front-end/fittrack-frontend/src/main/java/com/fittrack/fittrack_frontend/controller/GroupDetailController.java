package com.fittrack.fittrack_frontend.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;
import com.fittrack.fittrack_frontend.service.ApiClient;
import com.fittrack.fittrack_frontend.service.DashboardModels;
import com.fittrack.fittrack_frontend.service.NavigationService;

import java.util.ArrayList;
import java.util.List;

@Controller
public class GroupDetailController {
    private final ApiClient apiClient;
    private final NavigationService navigationService;

    public GroupDetailController(ApiClient apiClient, NavigationService navigationService) {
        this.apiClient = apiClient;
        this.navigationService = navigationService;
    }

    @FXML private Label lblNomeGrupo;
    @FXML private Label lblTagFoco;
    @FXML private PieChart chartVolume;
    @FXML private Label lblVolumeTotalFormatado;
    @FXML private GridPane gridLegenda;
    @FXML private HBox containerBarrasFrequencia;
    @FXML private Label lblTotalTreinos;
    @FXML private Label lblMediaSemanal;

    // Estruturas de Dados para Receber os Valores Reais
    public static class ExercisioVolumeData {
        String nome;
        double volumeKg;
        String corHex;

        public ExercisioVolumeData(String nome, double volumeKg, String corHex) {
            this.nome = nome;
            this.volumeKg = volumeKg;
            this.corHex = corHex;
        }
    }

    public static class FrequenciaMesData {
        String mes;
        int qtdTreinos;

        public FrequenciaMesData(String mes, int qtdTreinos) {
            this.mes = mes;
            this.qtdTreinos = qtdTreinos;
        }
    }

    @FXML
    public void initialize() {
        List<ExercisioVolumeData> exercicios = new ArrayList<>();
        try {
            DashboardModels.DashboardResposta dashboard = apiClient.get("/api/dashboard", DashboardModels.DashboardResposta.class);
            if (dashboard.volumePorGrupo() != null) {
                dashboard.volumePorGrupo().forEach((grupo, volume) -> exercicios.add(new ExercisioVolumeData(grupo, volume.doubleValue(), "#43D61B")));
            }
            carregarEMedirMetricasReais("Volume por grupo", "API", exercicios, List.of());
            return;
        } catch (Exception ignored) {
        }
        exercicios.add(new ExercisioVolumeData("Supino Reto", 3802.5, "#43D61B"));
        exercicios.add(new ExercisioVolumeData("Inclinado", 2112.5, "#2E7D32"));
        exercicios.add(new ExercisioVolumeData("Crucifixo", 1521.0, "#38B6FF"));
        exercicios.add(new ExercisioVolumeData("Crossover", 1014.0, "#888888"));

        List<FrequenciaMesData> frequencias = new ArrayList<>();
        frequencias.add(new FrequenciaMesData("Jun", 12));
        frequencias.add(new FrequenciaMesData("Jul", 15));
        frequencias.add(new FrequenciaMesData("Ago", 18));
        frequencias.add(new FrequenciaMesData("Set", 21));

        carregarEMedirMetricasReais("Peitoral", "Foco A", exercicios, frequencias);
    }

    public void carregarEMedirMetricasReais(String nomeGrupo, String tagFoco, 
                                           List<ExercisioVolumeData> exercicios, 
                                           List<FrequenciaMesData> frequencias) {
        lblNomeGrupo.setText(nomeGrupo);
        lblTagFoco.setText(tagFoco);

        // 1. CÁLCULO E MEDIÇÃO DO VOLUME TOTAL E PERCENTUAIS
        double volumeTotal = 0.0;
        for (ExercisioVolumeData item : exercicios) {
            volumeTotal += item.volumeKg;
        }

        lblVolumeTotalFormatado.setText(String.format("%,.0f", volumeTotal).replace(",", "."));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        gridLegenda.getChildren().clear();

        for (int i = 0; i < exercicios.size(); i++) {
            ExercisioVolumeData item = exercicios.get(i);
            double pct = volumeTotal > 0 ? (item.volumeKg / volumeTotal) * 100 : 0;

            pieData.add(new PieChart.Data(item.nome, item.volumeKg));

            // Legenda em 2 Colunas com Percentual Calculado
            HBox itemLegenda = new HBox(6.0);
            itemLegenda.setAlignment(Pos.CENTER_LEFT);

            Region dot = new Region();
            dot.getStyleClass().add("legend-dot");
            dot.setStyle("-fx-background-color: " + item.corHex + ";");

            Label lblText = new Label(String.format("%s (%.0f%%)", item.nome, pct));
            lblText.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 11px;");

            itemLegenda.getChildren().addAll(dot, lblText);

            int col = i % 2;
            int row = i / 2;
            gridLegenda.add(itemLegenda, col, row);
        }

        chartVolume.setData(pieData);

        // 2. CÁLCULO DA FREQUÊNCIA E BARRAS PROPORCIONAIS
        containerBarrasFrequencia.getChildren().clear();
        int totalTreinosAcumulado = 0;
        int maxTreinosMes = 1;

        for (FrequenciaMesData f : frequencias) {
            if (f.qtdTreinos > maxTreinosMes) maxTreinosMes = f.qtdTreinos;
            totalTreinosAcumulado += f.qtdTreinos;
        }

        for (FrequenciaMesData f : frequencias) {
            VBox barBox = new VBox(6.0);
            barBox.setAlignment(Pos.BOTTOM_CENTER);

            Label lblValor = new Label(String.valueOf(f.qtdTreinos));
            lblValor.setStyle("-fx-text-fill: #43D61B; -fx-font-weight: bold; -fx-font-size: 11px;");

            Region barRect = new Region();
            barRect.getStyleClass().add("chart-bar-rect");
            barRect.setPrefWidth(26.0);
            
            // Altura proporcional calculada dinamicamente (Max = 95px)
            double alturaCalculada = (double) f.qtdTreinos / maxTreinosMes * 95.0;
            barRect.setPrefHeight(Math.max(15.0, alturaCalculada));

            Label lblMes = new Label(f.mes);
            lblMes.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

            barBox.getChildren().addAll(lblValor, barRect, lblMes);
            containerBarrasFrequencia.getChildren().add(barBox);
        }

        // 3. KPI TOTAL E MÉDIA SEMANAL CALCULADA
        lblTotalTreinos.setText(totalTreinosAcumulado + " realizados");
        double mediaSemanalCalculada = (double) totalTreinosAcumulado / (frequencias.size() * 4);
        lblMediaSemanal.setText(String.format("%.1fx frequência", mediaSemanalCalculada));
    }

    @FXML
    private void handleVoltar() {
        navigationService.navegar("/fxml/metrics_hub_view.fxml");
    }
}
