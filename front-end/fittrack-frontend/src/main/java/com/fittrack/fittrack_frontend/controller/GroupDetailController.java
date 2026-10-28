package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.util.NavigationManager;

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

import java.util.List;

@Controller
public class GroupDetailController {

    @FXML private Label lblNomeGrupo;
    @FXML private Label lblTagFoco;
    @FXML private PieChart chartVolume;
    @FXML private Label lblVolumeTotalFormatado;
    @FXML private GridPane gridLegenda;
    @FXML private HBox containerBarrasFrequencia;
    @FXML private Label lblTotalTreinos;
    @FXML private Label lblMediaSemanal;

    // Estruturas de Dados para Transporte dos Dados Reais da API
    public static class ExercisioVolumeData {
        private final String nome;
        private final double volumeKg;
        private final String corHex;

        public ExercisioVolumeData(String nome, double volumeKg, String corHex) {
            this.nome = nome;
            this.volumeKg = volumeKg;
            this.corHex = corHex;
        }

        public String getNome() { return nome; }
        public double getVolumeKg() { return volumeKg; }
        public String getCorHex() { return corHex; }
    }

    public static class FrequenciaMesData {
        private final String mes;
        private final int qtdTreinos;

        public FrequenciaMesData(String mes, int qtdTreinos) {
            this.mes = mes;
            this.qtdTreinos = qtdTreinos;
        }

        public String getMes() { return mes; }
        public int getQtdTreinos() { return qtdTreinos; }
    }

    @FXML
    public void initialize() {
        if (chartVolume != null) chartVolume.getData().clear();
        if (gridLegenda != null) gridLegenda.getChildren().clear();
        if (containerBarrasFrequencia != null) containerBarrasFrequencia.getChildren().clear();
    }

    /**
     * Injeta e calcula dinamicamente as métricas do grupo muscular vindo da API/Sessão.
     */
    public void carregarEMedirMetricasReais(String nomeGrupo, String tagFoco, 
                                            List<ExercisioVolumeData> exercicios, 
                                            List<FrequenciaMesData> frequencias) {
        if (lblNomeGrupo != null) lblNomeGrupo.setText(nomeGrupo != null ? nomeGrupo : "Grupo Muscular");
        if (lblTagFoco != null) lblTagFoco.setText(tagFoco != null ? tagFoco : "");

        // 1. CÁLCULO E MEDIÇÃO DO VOLUME TOTAL E PERCENTUAIS
        double volumeTotal = 0.0;
        if (exercicios != null) {
            for (ExercisioVolumeData item : exercicios) {
                volumeTotal += item.getVolumeKg();
            }
        }

        if (lblVolumeTotalFormatado != null) {
            lblVolumeTotalFormatado.setText(String.format("%,.0f", volumeTotal).replace(",", "."));
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (gridLegenda != null) gridLegenda.getChildren().clear();

        if (exercicios != null && !exercicios.isEmpty()) {
            for (int i = 0; i < exercicios.size(); i++) {
                ExercisioVolumeData item = exercicios.get(i);
                double pct = volumeTotal > 0 ? (item.getVolumeKg() / volumeTotal) * 100 : 0;

                pieData.add(new PieChart.Data(item.getNome(), item.getVolumeKg()));

                if (gridLegenda != null) {
                    HBox itemLegenda = new HBox(6.0);
                    itemLegenda.setAlignment(Pos.CENTER_LEFT);

                    Region dot = new Region();
                    dot.getStyleClass().add("legend-dot");
                    String cor = (item.getCorHex() != null && !item.getCorHex().isBlank()) ? item.getCorHex() : "#43D61B";
                    dot.setStyle("-fx-background-color: " + cor + ";");

                    Label lblText = new Label(String.format("%s (%.0f%%)", item.getNome(), pct));
                    lblText.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 11px;");

                    itemLegenda.getChildren().addAll(dot, lblText);

                    int col = i % 2;
                    int row = i / 2;
                    gridLegenda.add(itemLegenda, col, row);
                }
            }
        }

        if (chartVolume != null) {
            chartVolume.setData(pieData);
        }

        // 2. CÁLCULO DA FREQUÊNCIA E BARRAS PROPORCIONAIS
        if (containerBarrasFrequencia != null) containerBarrasFrequencia.getChildren().clear();
        int totalTreinosAcumulado = 0;
        int maxTreinosMes = 1;

        if (frequencias != null && !frequencias.isEmpty()) {
            for (FrequenciaMesData f : frequencias) {
                if (f.getQtdTreinos() > maxTreinosMes) maxTreinosMes = f.getQtdTreinos();
                totalTreinosAcumulado += f.getQtdTreinos();
            }

            for (FrequenciaMesData f : frequencias) {
                VBox barBox = new VBox(6.0);
                barBox.setAlignment(Pos.BOTTOM_CENTER);

                Label lblValor = new Label(String.valueOf(f.getQtdTreinos()));
                lblValor.setStyle("-fx-text-fill: #43D61B; -fx-font-weight: bold; -fx-font-size: 11px;");

                Region barRect = new Region();
                barRect.getStyleClass().add("chart-bar-rect");
                barRect.setPrefWidth(26.0);

                double alturaCalculada = (double) f.getQtdTreinos() / maxTreinosMes * 95.0;
                barRect.setPrefHeight(Math.max(15.0, alturaCalculada));

                Label lblMes = new Label(f.getMes());
                lblMes.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

                barBox.getChildren().addAll(lblValor, barRect, lblMes);
                if (containerBarrasFrequencia != null) {
                    containerBarrasFrequencia.getChildren().add(barBox);
                }
            }
        }

        // 3. KPI TOTAL E MÉDIA SEMANAL CALCULADA
        if (lblTotalTreinos != null) {
            lblTotalTreinos.setText(totalTreinosAcumulado + " realizados");
        }

        if (lblMediaSemanal != null) {
            int numMeses = (frequencias != null && !frequencias.isEmpty()) ? frequencias.size() : 1;
            double mediaSemanalCalculada = (double) totalTreinosAcumulado / (numMeses * 4);
            lblMediaSemanal.setText(String.format("%.1fx frequência", mediaSemanalCalculada));
        }
    }

    @FXML
    private void handleVoltar() {
        if (lblNomeGrupo != null) {
            NavigationManager.navegarPara(lblNomeGrupo, "/fxml/group_detail_view.fxml");
        }
    }
}
