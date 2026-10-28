package com.fittrack.fittrack_frontend.controller;

import java.util.List;

import com.fittrack.fittrack_frontend.client.PraticanteApiClient;
import com.fittrack.fittrack_frontend.client.TreinoApiClient;
import com.fittrack.fittrack_frontend.dto.AuthResponseDTO;
import com.fittrack.fittrack_frontend.dto.EstatisticasGeraisDTO;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.GrupoMuscularEnum;
import com.fittrack.fittrack_frontend.dto.TreinoAtivoDTO;
import com.fittrack.fittrack_frontend.util.FxAsync;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
public class DashboardHomeController {

    @FXML private Label lblNomeUsuario;
    @FXML private Label lblTotalTreinos;
    @FXML private Label lblTotalExercicios;
    @FXML private Label lblTotalSeries;
    @FXML private Label lblVolumeTotal;

    @FXML private Button btnIniciarTreino;
    @FXML private HBox containerExerciciosMaisRealizados;

    private final TreinoApiClient treinoApiClient;
    private final PraticanteApiClient praticanteApiClient;

    public DashboardHomeController(TreinoApiClient treinoApiClient, PraticanteApiClient praticanteApiClient) {
        this.treinoApiClient = treinoApiClient;
        this.praticanteApiClient = praticanteApiClient;
    }

    /** Pacote de dados do dashboard buscado de uma só vez fora da thread da interface. */
    private record DadosDashboard(EstatisticasGeraisDTO stats, List<ExercicioDTO> maisRealizados) {}

    @FXML
    public void initialize() {
        if (containerExerciciosMaisRealizados != null) {
            containerExerciciosMaisRealizados.getChildren().clear();
        }

        AuthResponseDTO usuario = MainController.getUsuarioLogado();
        if (usuario == null || usuario.praticanteId() == null) return;

        Long praticanteId = usuario.praticanteId();
        FxAsync.executar(
                () -> new DadosDashboard(
                        praticanteApiClient.buscarEstatisticasGerais(praticanteId),
                        praticanteApiClient.listarExerciciosMaisRealizados(praticanteId)),
                dados -> carregarDadosDashboard(usuario.nome(), dados.stats(), dados.maisRealizados()),
                erro -> {
                    carregarDadosDashboard(usuario.nome(), null, List.of());
                    FxAsync.exibirErro("Não foi possível carregar o dashboard", erro);
                });
    }

    /** Exibe os dados reais recebidos da API Spring Boot. */
    public void carregarDadosDashboard(String nomeUsuario, EstatisticasGeraisDTO stats, List<ExercicioDTO> exerciciosMaisRealizados) {
        if (lblNomeUsuario != null && nomeUsuario != null && !nomeUsuario.isBlank()) {
            lblNomeUsuario.setText("Olá, " + nomeUsuario + " 👋");
        }

        if (stats != null) {
            if (lblTotalTreinos != null) lblTotalTreinos.setText(String.valueOf(stats.totalTreinos()));
            if (lblTotalExercicios != null) lblTotalExercicios.setText(String.valueOf(stats.totalExercicios()));
            if (lblTotalSeries != null) lblTotalSeries.setText(String.valueOf(stats.totalSeries()));
            if (lblVolumeTotal != null) lblVolumeTotal.setText(String.format("%,.0f kg", stats.volumeTotalAcumulado()));
        }

        renderizarExerciciosMaisRealizados(exerciciosMaisRealizados);
    }

    private void renderizarExerciciosMaisRealizados(List<ExercicioDTO> exercicios) {
        if (containerExerciciosMaisRealizados == null) return;
        containerExerciciosMaisRealizados.getChildren().clear();

        if (exercicios == null || exercicios.isEmpty()) {
            Label lblVazio = new Label("Nenhum exercício registrado ainda.");
            lblVazio.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
            containerExerciciosMaisRealizados.getChildren().add(lblVazio);
            return;
        }

        for (ExercicioDTO ex : exercicios) {
            VBox card = new VBox(6);
            card.getStyleClass().add("exercise-card");
            card.setPrefWidth(160.0);
            card.setAlignment(Pos.TOP_LEFT);

            Label lblNome = new Label(ex.nome());
            lblNome.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold; -fx-font-size: 14px;");

            Label lblInfo = new Label(GrupoMuscularEnum.rotuloDe(ex.grupoMuscular()));
            lblInfo.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 12px;");

            card.getChildren().addAll(lblNome, lblInfo);
            card.setOnMouseClicked(e -> handleVerDetalhesExercicio(ex));

            containerExerciciosMaisRealizados.getChildren().add(card);
        }
    }

    @FXML
    private void handleIniciarTreinoDoDia() {
        Long usuarioLogadoId = MainController.getUsuarioLogadoId();
        if (usuarioLogadoId == null) {
            FxAsync.exibirAviso("Sessão expirada", "Faça login novamente para iniciar o treino.");
            return;
        }

        Button origem = getNoReferencia();
        origem.setDisable(true);

        // Rotina real vinda do banco via API; nada de dados fixos no front-end.
        FxAsync.executar(
                () -> treinoApiClient.buscarTreinoDoDia(usuarioLogadoId),
                treino -> {
                    origem.setDisable(false);
                    abrirTreinoAtivo(origem, treino);
                },
                erro -> {
                    origem.setDisable(false);
                    FxAsync.exibirErro("Não foi possível carregar o treino do dia", erro);
                });
    }

    private void abrirTreinoAtivo(Button origem, TreinoAtivoDTO treino) {
        if (treino == null || treino.itens() == null || treino.itens().isEmpty()) {
            FxAsync.exibirAviso("Treino do dia", "Nenhum treino com exercícios foi encontrado para hoje.");
            return;
        }

        ActiveWorkoutController controller =
                NavigationManager.navegarComController(origem, "/fxml/active_workout_view.fxml");
        if (controller != null) {
            controller.carregarTreino(treino);
        }
    }

    @FXML
    private void handleVerTodosExercicios() {
        MetricsHubController controller =
                NavigationManager.navegarComController(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
        if (controller != null) controller.selecionarAbaExercicios();
    }

    private void handleVerDetalhesExercicio(ExercicioDTO exercicio) {
        if (exercicio == null) return;
        ExerciseDetailsController controller =
                NavigationManager.navegarComController(getNoReferencia(), "/fxml/exercise_details_view.fxml");
        if (controller != null) controller.carregarDetalhesExercicio(exercicio);
    }

    @FXML
    private void handleNavInicio() {
        // Já se encontra na tela inicial (Home)
    }

    @FXML
    private void handleNavTreinos() {
        MetricsHubController controller =
                NavigationManager.navegarComController(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
        if (controller != null) controller.selecionarAbaTreinos();
    }

    @FXML
    private void handleNavMetricas() {
        NavigationManager.navegarPara(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
    }

    private Button getNoReferencia() {
        return (btnIniciarTreino != null) ? btnIniciarTreino : new Button();
    }
}
