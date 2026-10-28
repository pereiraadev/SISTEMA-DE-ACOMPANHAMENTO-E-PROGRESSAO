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

/**
 * Controla {@code exercise_details_view.fxml}, mostrando dados, recordes e evolução do exercício.
 * A tela cria os cartões de histórico e o gráfico dinamicamente a partir do retorno da API.
 */
@Controller
@Scope("prototype")
public class ExerciseDetailsController {

    /** Nome do exercício selecionado na tela anterior. */
    @FXML private Label lblNomeExercicio;
    /** Grupo muscular exibido como rótulo localizado. */
    @FXML private Label lblGrupoMuscular;
    /** Estado de disponibilidade do exercício. */
    @FXML private Label lblStatus;
    /** Critério de ordenação atualmente aplicado ao histórico. */
    @FXML private Label lblFiltroAtual;

    /** Maior carga registrada para este exercício. */
    @FXML private Label lblMaiorCarga;
    /** Detalhe auxiliar do recorde de carga. */
    @FXML private Label lblMaiorCargaDet;
    /** Repetições associadas ao recorde de carga. */
    @FXML private Label lblMaiorReps;
    /** Detalhe auxiliar do recorde de repetições. */
    @FXML private Label lblMaiorRepsDet;

    /** Gráfico de linha atualizado com a carga histórica em ordem cronológica. */
    @FXML private LineChart<String, Number> chartEvolucao;
    /** Lista vertical reconstruída com os cartões do histórico filtrado. */
    @FXML private VBox containerHistorico;

    // Elementos do Bottom Sheet / Modal de Filtro
    /** Sobreposição que abre e fecha o seletor de ordenação. */
    @FXML private StackPane modalOverlay;
    /** Agrupa as opções para garantir uma única ordenação selecionada. */
    @FXML private ToggleGroup ordenacaoGroup;
    /** Opção para exibir primeiro os registros mais recentes. */
    @FXML private RadioButton optRecentes;
    /** Opção para exibir primeiro os registros mais antigos. */
    @FXML private RadioButton optAntigos;
    /** Opção para ordenar pela maior carga. */
    @FXML private RadioButton optMaiorCarga;
    /** Opção para ordenar pela menor carga. */
    @FXML private RadioButton optMenorCarga;
    /** Opção para ordenar pela maior quantidade de repetições. */
    @FXML private RadioButton optMaisReps;
    /** Opção para ordenar pela menor quantidade de repetições. */
    @FXML private RadioButton optMenosReps;
    /** Opção para ordenar pelo maior volume. */
    @FXML private RadioButton optMaiorVolume;
    /** Opção para ordenar pelo menor volume. */
    @FXML private RadioButton optMenorVolume;

    /** Cliente REST para carregar o histórico do exercício selecionado. */
    private final PraticanteApiClient praticanteApiClient;

    /** Identificador encaminhado para permitir que outras telas consultem a seleção atual. */
    private Long exercicioId;
    /** Histórico na ordem recebida da API (mais recente primeiro). */
    private final List<HistoricoExercicioItemDTO> historicoOriginal = new ArrayList<>();
    /** Histórico exibido (após ordenação escolhida no filtro). */
    private final List<HistoricoExercicioItemDTO> listaHistorico = new ArrayList<>();

    /**
     * Cria a tela com o cliente usado para buscar execuções históricas.
     *
     * @param praticanteApiClient cliente REST de dados do praticante
     */
    public ExerciseDetailsController(PraticanteApiClient praticanteApiClient) {
        this.praticanteApiClient = praticanteApiClient;
    }

    /** Limpa elementos dinâmicos e mantém o modal fechado ao carregar uma nova instância da view. */
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
     *
     * @param exercicio exercício selecionado na navegação de origem
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

    /**
     * Substitui o estado local e refaz o gráfico em ordem temporal, mantendo a lista pronta para filtros.
     *
     * @param historico dados recebidos da API, ou lista vazia antes do carregamento
     */
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

    /** Cria cards para o filtro atual; remove primeiro os filhos antigos para evitar duplicação. */
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

    /** Relaciona o texto de evolução da API às classes CSS conhecidas pela folha de estilos. */
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
        // Evento de clique: marca overlay como visível e gerenciado para ocupar espaço no layout.
        if (modalOverlay != null) {
            modalOverlay.setManaged(true);
            modalOverlay.setVisible(true);
        }
    }

    @FXML
    private void handleFecharFiltro() {
        // Botão/ação de cancelar fecha o bottom sheet sem alterar a ordenação aplicada.
        fecharModalFiltro();
    }

    /** Altera visibilidade e managed em conjunto para não reservar espaço quando o modal está fechado. */
    private void fecharModalFiltro() {
        if (modalOverlay != null) {
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
        }
    }

    @FXML
    private void handleAplicarFiltro() {
        // Lê o RadioButton selecionado, ordena uma cópia lógica da lista e atualiza os cards visíveis.
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
        // Retorna à tela de métricas; o FXML anterior é carregado pelo NavigationManager.
        if (containerHistorico != null) {
            NavigationManager.navegarPara(containerHistorico, "/fxml/metrics_hub_view.fxml");
        }
    }

    /**
     * Retorna o identificador do exercício atualmente exibido.
     *
     * @return chave do exercício ou {@code null} antes de receber os dados de navegação
     */
    public Long getExercicioId() {
        return exercicioId;
    }
}
