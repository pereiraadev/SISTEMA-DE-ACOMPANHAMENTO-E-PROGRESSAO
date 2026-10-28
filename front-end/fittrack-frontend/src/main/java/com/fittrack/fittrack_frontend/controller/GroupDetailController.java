package com.fittrack.fittrack_frontend.controller;

import java.util.List;

import com.fittrack.fittrack_frontend.client.PraticanteApiClient;
import com.fittrack.fittrack_frontend.dto.DesempenhoGrupoDTO;
import com.fittrack.fittrack_frontend.dto.DetalheGrupoDTO;
import com.fittrack.fittrack_frontend.dto.FrequenciaMesDTO;
import com.fittrack.fittrack_frontend.dto.GrupoMuscularEnum;
import com.fittrack.fittrack_frontend.dto.VolumeExercicioDTO;
import com.fittrack.fittrack_frontend.util.FxAsync;
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

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class GroupDetailController {

    /** Paleta das fatias do gráfico (a API não precisa conhecer cores de UI). */
    private static final List<String> PALETA = List.of(
            "#43D61B", "#2E9BFF", "#FFB020", "#FF5C8A", "#A78BFA", "#22D3EE");

    @FXML private Label lblNomeGrupo;
    @FXML private Label lblTagFoco;
    @FXML private PieChart chartVolume;
    @FXML private Label lblVolumeTotalFormatado;
    @FXML private GridPane gridLegenda;
    @FXML private HBox containerBarrasFrequencia;
    @FXML private Label lblTotalTreinos;
    @FXML private Label lblMediaSemanal;

    private final PraticanteApiClient praticanteApiClient;

    public GroupDetailController(PraticanteApiClient praticanteApiClient) {
        this.praticanteApiClient = praticanteApiClient;
    }

    @FXML
    public void initialize() {
        if (chartVolume != null) chartVolume.getData().clear();
        if (gridLegenda != null) gridLegenda.getChildren().clear();
        if (containerBarrasFrequencia != null) containerBarrasFrequencia.getChildren().clear();
    }

    /**
     * Abre o detalhe a partir do card da aba "Grupos Musculares": mostra o nome imediatamente
     * e busca na API o volume por exercício e a frequência mensal.
     */
    public void carregarGrupo(DesempenhoGrupoDTO grupo) {
        if (grupo == null) return;

        if (lblNomeGrupo != null) lblNomeGrupo.setText(GrupoMuscularEnum.rotuloDe(grupo.grupoMuscular()));
        if (lblTagFoco != null) lblTagFoco.setText("");

        Long praticanteId = MainController.getUsuarioLogadoId();
        if (praticanteId == null) return;

        FxAsync.executar(
                () -> praticanteApiClient.buscarDetalheGrupo(praticanteId, grupo.grupoMuscular()),
                this::carregarDetalhe,
                erro -> FxAsync.exibirErro("Não foi possível carregar o grupo muscular", erro));
    }

    /** Calcula e renderiza as métricas do grupo com os dados reais da API. */
    public void carregarDetalhe(DetalheGrupoDTO detalhe) {
        if (detalhe == null) return;

        if (lblNomeGrupo != null && detalhe.grupoMuscular() != null) {
            lblNomeGrupo.setText(GrupoMuscularEnum.rotuloDe(detalhe.grupoMuscular()));
        }
        if (lblTagFoco != null) lblTagFoco.setText(detalhe.tagFoco() != null ? detalhe.tagFoco() : "");

        renderizarVolume(detalhe.volumePorExercicio());
        renderizarFrequencia(detalhe.frequenciaMensal());
    }

    private void renderizarVolume(List<VolumeExercicioDTO> exercicios) {
        double volumeTotal = 0.0;
        if (exercicios != null) {
            for (VolumeExercicioDTO item : exercicios) {
                volumeTotal += valor(item.volumeKg());
            }
        }

        if (lblVolumeTotalFormatado != null) {
            lblVolumeTotalFormatado.setText(String.format("%,.0f", volumeTotal).replace(",", "."));
        }

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        if (gridLegenda != null) gridLegenda.getChildren().clear();

        if (exercicios != null) {
            for (int i = 0; i < exercicios.size(); i++) {
                VolumeExercicioDTO item = exercicios.get(i);
                double volume = valor(item.volumeKg());
                double pct = volumeTotal > 0 ? (volume / volumeTotal) * 100 : 0;
                String cor = PALETA.get(i % PALETA.size());

                pieData.add(new PieChart.Data(item.nome(), volume));

                if (gridLegenda != null) {
                    HBox itemLegenda = new HBox(6.0);
                    itemLegenda.setAlignment(Pos.CENTER_LEFT);

                    Region dot = new Region();
                    dot.getStyleClass().add("legend-dot");
                    dot.setStyle("-fx-background-color: " + cor + ";");

                    Label lblText = new Label(String.format("%s (%.0f%%)", item.nome(), pct));
                    lblText.setStyle("-fx-text-fill: #CCCCCC; -fx-font-size: 11px;");

                    itemLegenda.getChildren().addAll(dot, lblText);
                    gridLegenda.add(itemLegenda, i % 2, i / 2);
                }
            }
        }

        if (chartVolume != null) {
            chartVolume.setData(pieData);
            // Aplica as mesmas cores da legenda às fatias (após o layout criar os nós).
            for (int i = 0; i < pieData.size(); i++) {
                String cor = PALETA.get(i % PALETA.size());
                PieChart.Data fatia = pieData.get(i);
                fatia.nodeProperty().addListener((obs, antigo, no) -> {
                    if (no != null) no.setStyle("-fx-pie-color: " + cor + ";");
                });
                if (fatia.getNode() != null) fatia.getNode().setStyle("-fx-pie-color: " + cor + ";");
            }
        }
    }

    private void renderizarFrequencia(List<FrequenciaMesDTO> frequencias) {
        if (containerBarrasFrequencia != null) containerBarrasFrequencia.getChildren().clear();

        int totalTreinos = 0;
        int maxTreinosMes = 1;

        if (frequencias != null && !frequencias.isEmpty()) {
            for (FrequenciaMesDTO f : frequencias) {
                int qtd = f.qtdTreinos() != null ? f.qtdTreinos() : 0;
                maxTreinosMes = Math.max(maxTreinosMes, qtd);
                totalTreinos += qtd;
            }

            for (FrequenciaMesDTO f : frequencias) {
                int qtd = f.qtdTreinos() != null ? f.qtdTreinos() : 0;

                VBox barBox = new VBox(6.0);
                barBox.setAlignment(Pos.BOTTOM_CENTER);

                Label lblValor = new Label(String.valueOf(qtd));
                lblValor.setStyle("-fx-text-fill: #43D61B; -fx-font-weight: bold; -fx-font-size: 11px;");

                Region barRect = new Region();
                barRect.getStyleClass().add("chart-bar-rect");
                barRect.setPrefWidth(26.0);
                barRect.setPrefHeight(Math.max(15.0, (double) qtd / maxTreinosMes * 95.0));

                Label lblMes = new Label(f.mes());
                lblMes.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");

                barBox.getChildren().addAll(lblValor, barRect, lblMes);
                if (containerBarrasFrequencia != null) containerBarrasFrequencia.getChildren().add(barBox);
            }
        }

        if (lblTotalTreinos != null) {
            lblTotalTreinos.setText(totalTreinos + " realizados");
        }
        if (lblMediaSemanal != null) {
            int numMeses = (frequencias != null && !frequencias.isEmpty()) ? frequencias.size() : 1;
            double mediaSemanal = (double) totalTreinos / (numMeses * 4);
            lblMediaSemanal.setText(String.format("%.1fx frequência", mediaSemanal));
        }
    }

    private static double valor(Double v) {
        return v != null ? v : 0.0;
    }

    @FXML
    private void handleVoltar() {
        if (lblNomeGrupo != null) {
            NavigationManager.navegarPara(lblNomeGrupo, "/fxml/metrics_hub_view.fxml");
        }
    }
}
