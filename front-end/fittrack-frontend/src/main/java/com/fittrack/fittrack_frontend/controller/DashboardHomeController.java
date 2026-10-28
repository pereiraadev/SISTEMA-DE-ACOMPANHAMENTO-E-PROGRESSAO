package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ChoiceDialog;
import java.util.List;
import org.springframework.stereotype.Controller;
import com.fittrack.fittrack_frontend.service.ApiClient;
import com.fittrack.fittrack_frontend.service.NavigationService;
import com.fittrack.fittrack_frontend.service.DashboardModels;
import com.fittrack.fittrack_frontend.service.WorkoutModels;

@Controller
public class DashboardHomeController {
    @FXML private Label lblTotalTreinos;
    @FXML private Label lblTotalExercicios;
    @FXML private Label lblTotalSeries;
    @FXML private Label lblVolumeTotal;
    @FXML private javafx.scene.text.Text txtSaudacao;
    private final com.fittrack.fittrack_frontend.service.PractitionerSession practitionerSession;
    private final ApiClient apiClient;
    private final NavigationService navigationService;
    private final com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession;

    public DashboardHomeController(ApiClient apiClient, NavigationService navigationService,
                                   com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession,
                                   com.fittrack.fittrack_frontend.service.PractitionerSession practitionerSession) {
        this.apiClient = apiClient;
        this.navigationService = navigationService;
        this.workoutSession = workoutSession;
        this.practitionerSession = practitionerSession;
    }

    @FXML
    private void initialize() {
        txtSaudacao.setText("Olá, " + practitionerSession.obterNome());
        try {
            DashboardModels.DashboardResposta dashboard = apiClient.get("/api/dashboard", DashboardModels.DashboardResposta.class);
            lblTotalTreinos.setText(String.valueOf(dashboard.sessoesConcluidas()));
            lblVolumeTotal.setText(String.format("%,.0f kg", dashboard.volumeTotal()));
            lblTotalExercicios.setText(String.valueOf(dashboard.totalExercicios()));
            lblTotalSeries.setText(String.valueOf(dashboard.totalSeries()));
        } catch (Exception erro) {
            lblTotalTreinos.setText("—");
            lblTotalExercicios.setText("—");
            lblTotalSeries.setText("—");
            lblVolumeTotal.setText("Erro ao carregar");
        }
    }

    @FXML
    private void handleIniciarTreinoDoDia() {
        try {
            java.util.List<WorkoutModels.TreinoResposta> modelos = apiClient.getList("/api/modelos", new com.fasterxml.jackson.core.type.TypeReference<>() {});
            if (modelos.isEmpty()) {
                mostrarAviso("Nenhum modelo de treino disponível.");
                return;
            }
            List<String> opcoes = modelos.stream().map(WorkoutModels.TreinoResposta::identificacao).toList();
            ChoiceDialog<String> dialog = new ChoiceDialog<>(opcoes.get(0), opcoes);
            dialog.setTitle("Selecionar treino");
            dialog.setHeaderText("Escolha o treino para iniciar");
            var escolha = dialog.showAndWait();
            if (escolha.isEmpty()) return;
            WorkoutModels.TreinoResposta modelo = modelos.get(opcoes.indexOf(escolha.get()));
            WorkoutModels.TreinoResposta execucao = apiClient.post("/api/modelos/" + modelo.id() + "/iniciar", null, WorkoutModels.TreinoResposta.class);
            workoutSession.definir(execucao);
            navigationService.navegar("/fxml/active_workout_view.fxml");
        } catch (Exception erro) {
            mostrarAviso("Não foi possível iniciar o treino.");
        }
    }

    private void mostrarAviso(String mensagem) {
        new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION, mensagem).showAndWait();
    }

    @FXML
    private void handleVerTodosExercicios() {
        navigationService.navegar("/fxml/metrics_hub_view.fxml");
    }

    @FXML
    private void handleNavInicio() {
        // Já está na tela Home
    }

    @FXML
    private void handleNavTreinos() {
        navigationService.navegar("/fxml/workout_models_view.fxml");
    }

    @FXML
    private void handleNavMetricas() {
        navigationService.navegar("/fxml/metrics_hub_view.fxml");
    }
}
