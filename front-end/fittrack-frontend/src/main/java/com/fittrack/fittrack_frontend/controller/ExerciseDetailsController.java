package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.HistoricoExercicioItemDTO;
import com.fittrack.fittrack_frontend.util.NavigationManager;

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

    @FXML private Label lblNomeExercicio;
    @FXML private Label lblNomeExercício; // Suporte a variação com acento no FXML
    @FXML private Label lblGrupoMuscular;
    @FXML private Label lblStatus;
    @FXML private Label lblFiltroAtual;

    @FXML private Label lblMaiorCarga;
    @FXML private Label lblMaiorCargaDet;
    @FXML private Label lblMaiorReps;
    @FXML private Label lblMaiorRepsDet;

    @FXML private LineChart<String, Number> chartEvolucao;
    @FXML private VBox containerHistorico;

    // Elementos do Bottom Sheet / Modal de Filtro
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

    private Long exercicioId;
    private final List<HistoricoExercicioItemDTO> listaHistorico = new ArrayList<>();

    @FXML
    public void initialize() {
        if (containerHistorico != null) {
            containerHistorico.getChildren().clear();
        }
        if (chartEvolucao != null) {
            chartEvolucao.getData().clear();
        }
        fecharModalFiltro();
    }

    /**
     * Injeta os dados do exercício e seu histórico temporal de execuções trazidos da API Spring Boot.
     */
    public void carregarDetalhesExercicio(ExercicioDTO exercicio, List<HistoricoExercicioItemDTO> historico, XYChart.Series<String, Number> dadosGrafico) {
        if (exercicio != null) {
            this.exercicioId = exercicio.id();

            String nome = exercicio.nome();
            if (lblNomeExercicio != null) lblNomeExercicio.setText(nome);
            if (lblNomeExercício != null) lblNomeExercício.setText(nome);

            if (lblGrupoMuscular != null) {
                lblGrupoMuscular.setText(exercicio.grupoMuscular() != null ? exercicio.grupoMuscular() : "Geral");
            }

            if (lblStatus != null) {
                lblStatus.setText(exercicio.ativo() ? "Ativo" : "Inativo");
            }

            // Mapeamento dos Recordes Pessoais (PRs) do Exercício
            if (lblMaiorCarga != null && exercicio.recordeCarga() != null) {
                lblMaiorCarga.setText(String.format("%.0f kg", exercicio.recordeCarga()));
            }

            if (lblMaiorReps != null && exercicio.recordeRepsParaCarga() != null) {
                lblMaiorReps.setText(exercicio.recordeRepsParaCarga() + " reps");
            }
        }

        // Atualização do Gráfico de Evolução
        if (chartEvolucao != null) {
            chartEvolucao.getData().clear();
            if (dadosGrafico != null) {
                chartEvolucao.getData().add(dadosGrafico);
            }
        }

        // Atualização da Lista do Histórico
        this.listaHistorico.clear();
        if (historico != null) {
            this.listaHistorico.addAll(historico);
        }

        renderizarHistorico();
    }

    private void renderizarHistorico() {
        if (containerHistorico == null) return;
        containerHistorico.getChildren().clear();

        if (listaHistorico.isEmpty()) {
            Label lblVazio = new Label("Nenhum registro no histórico deste exercício.");
            lblVazio.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
            containerHistorico.getChildren().add(lblVazio);
            return;
        }

        for (HistoricoExercicioItemDTO item : listaHistorico) {
            HBox card = new HBox(10.0);
            card.getStyleClass().add("history-card");
            card.setAlignment(Pos.CENTER_LEFT);

            VBox info = new VBox(2.0);
            Label lblData = new Label(item.dataTreino() != null ? item.dataTreino() : "—");
            lblData.getStyleClass().add("history-date");

            String desc = String.format("%s • %.0f kg • Vol: %,.0f kg",
                    (item.descricaoSeries() != null ? item.descricaoSeries() : "Séries"),
                    item.carga(),
                    item.volumeCalculado());
            Label lblDesc = new Label(desc);
            lblDesc.getStyleClass().add("history-desc");

            info.getChildren().addAll(lblData, lblDesc);
            HBox.setHgrow(info, Priority.ALWAYS);

            // Mapeamento dinâmico da tag de status de evolução
            String status = item.statusEvolucao() != null ? item.statusEvolucao() : "Manutenção";
            Label tag = new Label(status);
            tag.getStyleClass().add(obterCssClassParaStatus(status));

            card.getChildren().addAll(info, tag);
            containerHistorico.getChildren().add(card);
        }
    }

    private String obterCssClassParaStatus(String status) {
        String lower = status.toLowerCase();
        if (lower.contains("aumento") || lower.contains("carga")) {
            return "tag-aumento";
        } else if (lower.contains("reps") || lower.contains("evolu")) {
            return "tag-reps";
        } else if (lower.contains("redu")) {
            return "tag-reducao";
        }
        return "tag-manutencao";
    }

    @FXML
    private void handleAbrirFiltro() {
        if (modalOverlay != null) {
            modalOverlay.setManaged(true);
            modalOverlay.setVisible(true);
        }
    }

    @FXML
    private void handleFecharFiltro() {
        fecharModalFiltro();
    }

    private void fecharModalFiltro() {
        if (modalOverlay != null) {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        }
    }

    @FXML
    private void handleAplicarFiltro() {
        if (ordenacaoGroup == null) {
            fecharModalFiltro();
            return;
        }

        RadioButton selected = (RadioButton) ordenacaoGroup.getSelectedToggle();
        if (selected == null) {
            fecharModalFiltro();
            return;
        }

        if (selected == optRecentes) {
            if (lblFiltroAtual != null) lblFiltroAtual.setText("Data ⤓");
        } else if (selected == optAntigos) {
            if (lblFiltroAtual != null) lblFiltroAtual.setText("Data ⤒");
            Collections.reverse(listaHistorico);
        } else if (selected == optMaiorCarga) {
            if (lblFiltroAtual != null) lblFiltroAtual.setText("Carga ⤓");
            listaHistorico.sort((a, b) -> Double.compare(b.carga(), a.carga()));
        } else if (selected == optMenorCarga) {
            if (lblFiltroAtual != null) lblFiltroAtual.setText("Carga ⤒");
            listaHistorico.sort((a, b) -> Double.compare(a.carga(), b.carga()));
        } else if (selected == optMaiorVolume) {
            if (lblFiltroAtual != null) lblFiltroAtual.setText("Volume ⤓");
            listaHistorico.sort((a, b) -> Double.compare(b.volumeCalculado(), a.volumeCalculado()));
        } else if (selected == optMenorVolume) {
            if (lblFiltroAtual != null) lblFiltroAtual.setText("Volume ⤒");
            listaHistorico.sort((a, b) -> Double.compare(a.volumeCalculado(), b.volumeCalculado()));
        }

        renderizarHistorico();
        fecharModalFiltro();
    }

    @FXML
    private void handleVoltar() {
        if (containerHistorico != null) {
            NavigationManager.navegarPara(containerHistorico, "/fxml/metrics_hub_view.fxml");
        }
    }

    public Long getExercicioId() {
        return exercicioId;
    }
}
