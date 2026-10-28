package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.client.PraticanteApiClient;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.GrupoMuscularEnum;
import com.fittrack.fittrack_frontend.dto.HistoricoExercicioItemDTO;
import com.fittrack.fittrack_frontend.util.FxAsync;
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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@Scope("prototype")
public class ExerciseDetailsController {

    @FXML private Label lblNomeExercicio;
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

    private final PraticanteApiClient praticanteApiClient;

    private Long exercicioId;
    /** Histórico na ordem recebida da API (mais recente primeiro). */
    private final List<HistoricoExercicioItemDTO> historicoOriginal = new ArrayList<>();
    /** Histórico exibido (após ordenação escolhida no filtro). */
    private final List<HistoricoExercicioItemDTO> listaHistorico = new ArrayList<>();

    public ExerciseDetailsController(PraticanteApiClient praticanteApiClient) {
        this.praticanteApiClient = praticanteApiClient;
    }

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
     * Exibe os dados do exercício (já conhecidos pelo ExercicioDTO) e busca na API o histórico
     * de execuções para a lista e o gráfico de evolução.
     */
    public void carregarDetalhesExercicio(ExercicioDTO exercicio) {
        if (exercicio == null) return;
        this.exercicioId = exercicio.id();

        if (lblNomeExercicio != null) lblNomeExercicio.setText(exercicio.nome());
        if (lblGrupoMuscular != null) lblGrupoMuscular.setText(GrupoMuscularEnum.rotuloDe(exercicio.grupoMuscular()));
        if (lblStatus != null) lblStatus.setText(Boolean.FALSE.equals(exercicio.ativo()) ? "Inativo" : "Ativo");

        // Recordes pessoais (campos recorde_* de EXERCICIO)
        if (lblMaiorCarga != null && exercicio.recordeCarga() != null) {
            lblMaiorCarga.setText(String.format("%.0f kg", exercicio.recordeCarga()));
        }
        if (lblMaiorReps != null && exercicio.recordeRepsParaCarga() != null) {
            lblMaiorReps.setText(exercicio.recordeRepsParaCarga() + " reps");
        }

        aplicarHistorico(List.of());

        Long praticanteId = MainController.getUsuarioLogadoId();
        if (praticanteId == null || exercicio.id() == null) return;

        FxAsync.executar(
                () -> praticanteApiClient.listarHistoricoExercicio(praticanteId, exercicio.id()),
                this::aplicarHistorico,
                erro -> FxAsync.exibirErro("Não foi possível carregar o histórico do exercício", erro));
    }

    private void aplicarHistorico(List<HistoricoExercicioItemDTO> historico) {
        historicoOriginal.clear();
        if (historico != null) historicoOriginal.addAll(historico);

        listaHistorico.clear();
        listaHistorico.addAll(historicoOriginal);

        // Gráfico de evolução: carga por data (da mais antiga para a mais recente).
        if (chartEvolucao != null) {
            chartEvolucao.getData().clear();
            if (!historicoOriginal.isEmpty()) {
                XYChart.Series<String, Number> serie = new XYChart.Series<>();
                for (int i = historicoOriginal.size() - 1; i >= 0; i--) {
                    HistoricoExercicioItemDTO h = historicoOriginal.get(i);
                    serie.getData().add(new XYChart.Data<>(h.dataTreino() != null ? h.dataTreino() : "—", h.carga()));
                }
                chartEvolucao.getData().add(serie);
            }
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
            listaHistorico.clear();
            listaHistorico.addAll(historicoOriginal);
        } else if (selected == optAntigos) {
            if (lblFiltroAtual != null) lblFiltroAtual.setText("Data ⤒");
            listaHistorico.clear();
            listaHistorico.addAll(historicoOriginal);
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
