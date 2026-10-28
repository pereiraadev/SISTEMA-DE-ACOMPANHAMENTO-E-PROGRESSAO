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

/**
 * Controla o dashboard inicial em {@code dashboard_home_view.fxml}, com resumo do praticante,
 * exercícios mais realizados e entrada para a sessão de treino do dia.
 */
@Controller
@Scope("prototype")
public class DashboardHomeController {

    /** Saudação personalizada do praticante autenticado. */
    @FXML private Label lblNomeUsuario;
    /** Indicador de sessões contabilizadas no histórico. */
    @FXML private Label lblTotalTreinos;
    /** Indicador de exercícios realizados. */
    @FXML private Label lblTotalExercicios;
    /** Indicador de séries registradas. */
    @FXML private Label lblTotalSeries;
    /** Volume acumulado apresentado em quilogramas. */
    @FXML private Label lblVolumeTotal;

    /** Botão de início da sessão do dia; também serve como nó para navegação. */
    @FXML private Button btnIniciarTreino;
    /** Área horizontal preenchida com cards de exercícios em tempo de execução. */
    @FXML private HBox containerExerciciosMaisRealizados;

    /** Cliente das operações de sessão de treino. */
    private final TreinoApiClient treinoApiClient;
    /** Cliente das consultas de estatísticas e exercícios. */
    private final PraticanteApiClient praticanteApiClient;

    /**
     * Injeta os clientes usados pelo dashboard para carregar resumo e sessão diária.
     *
     * @param treinoApiClient cliente de sessões de treino
     * @param praticanteApiClient cliente de dados do praticante
     */
    public DashboardHomeController(TreinoApiClient treinoApiClient, PraticanteApiClient praticanteApiClient) {
        this.treinoApiClient = treinoApiClient;
        this.praticanteApiClient = praticanteApiClient;
    }

    /** Dados do dashboard buscados conjuntamente fora da thread da interface. */
    private record DadosDashboard(
            /** Indicadores consolidados para os cartões do dashboard. */
            EstatisticasGeraisDTO stats,
            /** Exercícios que serão exibidos como destaques. */
            List<ExercicioDTO> maisRealizados) {}

    /** Limpa os nós antigos e inicia o carregamento assíncrono após o FXMLLoader injetar os componentes. */
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

    /**
     * Atualiza os rótulos e cards com dados retornados pela API.
     *
     * @param nomeUsuario nome apresentado na saudação
     * @param stats estatísticas consolidadas; pode ser nulo em uma falha parcial
     * @param exerciciosMaisRealizados lista dos exercícios destacados
     */
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

    /** Recria os cards JavaFX com os exercícios e instala o evento de clique de cada card. */
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
        // O clique valida a sessão local antes de pedir à API para criar ou retomar o treino.
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

    /** Valida se há itens executáveis, navega e passa o DTO carregado ao controller de destino. */
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
        // Navega para o hub e seleciona a aba correspondente no controller recém-carregado.
        MetricsHubController controller =
                NavigationManager.navegarComController(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
        if (controller != null) controller.selecionarAbaExercicios();
    }

    private void handleVerDetalhesExercicio(ExercicioDTO exercicio) {
        // O card encaminha o exercício selecionado à tela de histórico, sem nova busca por identidade.
        if (exercicio == null) return;
        ExerciseDetailsController controller =
                NavigationManager.navegarComController(getNoReferencia(), "/fxml/exercise_details_view.fxml");
        if (controller != null) controller.carregarDetalhesExercicio(exercicio);
    }

    @FXML
    private void handleNavInicio() {
        // A ação de navegação não altera a Scene porque esta já é a tela inicial.
    }

    @FXML
    private void handleNavTreinos() {
        // Abre a aba Treinos do hub, preservando a intenção expressa pelo item da navegação.
        MetricsHubController controller =
                NavigationManager.navegarComController(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
        if (controller != null) controller.selecionarAbaTreinos();
    }

    @FXML
    private void handleNavMetricas() {
        // Acessa o hub de métricas com a aba padrão configurada pelo seu controller.
        NavigationManager.navegarPara(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
    }

    /** Fornece um nó da cena atual para a troca de Scene centralizada no NavigationManager. */
    private Button getNoReferencia() {
        return (btnIniciarTreino != null) ? btnIniciarTreino : new Button();
    }
}
