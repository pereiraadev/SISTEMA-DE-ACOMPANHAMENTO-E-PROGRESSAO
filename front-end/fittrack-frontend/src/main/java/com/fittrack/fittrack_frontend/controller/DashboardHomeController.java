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
    private final ApiClient apiClient;
    private final NavigationService navigationService;
    private final com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession;

    public DashboardHomeController(ApiClient apiClient, NavigationService navigationService,
                                   com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession) {
        this.apiClient = apiClient;
        this.navigationService = navigationService;
        this.workoutSession = workoutSession;
    }

    @FXML
    private void initialize() {
        try {
            DashboardModels.DashboardResposta dashboard = apiClient.get("/api/dashboard", DashboardModels.DashboardResposta.class);
            lblTotalTreinos.setText(String.valueOf(dashboard.sessoesConcluidas()));
            lblVolumeTotal.setText(String.format("%,.0f kg", dashboard.volumeTotal()));
            int exercicios = dashboard.volumePorGrupo() == null ? 0 : dashboard.volumePorGrupo().size();
            lblTotalExercicios.setText(String.valueOf(exercicios));
            lblTotalSeries.setText("—");
        } catch (Exception ignored) {
        }
    }

    @FXML
    private void handleIniciarTreinoDoDia() {
        try {
            java.util.List<WorkoutModels.TreinoResposta> modelos = apiClient.getList("/api/modelos", new com.fasterxml.jackson.core.type.TypeReference<>() {});
            if (modelos.isEmpty()) return;
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
            return;
        }
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
        if (workoutSession.obter() != null) navigationService.navegar("/fxml/active_workout_view.fxml");
    }

    @FXML
    private void handleNavMetricas() {
        navigationService.navegar("/fxml/metrics_hub_view.fxml");
    }
}
