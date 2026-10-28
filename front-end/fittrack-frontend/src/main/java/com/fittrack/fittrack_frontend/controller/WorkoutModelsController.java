package com.fittrack.fittrack_frontend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fittrack.fittrack_frontend.service.ApiClient;
import com.fittrack.fittrack_frontend.service.NavigationService;
import com.fittrack.fittrack_frontend.service.WorkoutModels;
import com.fittrack.fittrack_frontend.service.WorkoutSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class WorkoutModelsController {
    @FXML private VBox containerModelos;
    @FXML private TextField inputIdentificacao;
    @FXML private Label lblErro;
    private final ApiClient apiClient;
    private final NavigationService navigationService;
    private final WorkoutSession workoutSession;

    public WorkoutModelsController(ApiClient apiClient, NavigationService navigationService, WorkoutSession workoutSession) {
        this.apiClient = apiClient;
        this.navigationService = navigationService;
        this.workoutSession = workoutSession;
    }

    @FXML public void initialize() { carregar(); }

    private void carregar() {
        containerModelos.getChildren().clear();
        try {
            List<WorkoutModels.TreinoResposta> modelos = apiClient.getList("/api/modelos", new TypeReference<>() {});
            if (modelos.isEmpty()) containerModelos.getChildren().add(new Label("Nenhum modelo cadastrado."));
            modelos.forEach(this::adicionarModelo);
        } catch (Exception erro) { mostrarErro("Não foi possível carregar os modelos."); }
    }

    private void adicionarModelo(WorkoutModels.TreinoResposta modelo) {
        javafx.scene.control.Button botao = new javafx.scene.control.Button(modelo.identificacao() + "  •  Iniciar");
        botao.setMaxWidth(Double.MAX_VALUE);
        botao.setOnAction(event -> iniciar(modelo.id()));
        containerModelos.getChildren().add(botao);
    }

    @FXML private void handleCriarModelo() {
        String identificacao = inputIdentificacao.getText().trim();
        if (identificacao.isBlank()) { mostrarErro("Informe o nome do modelo."); return; }
        try {
            WorkoutModels.ItemModelo item = new WorkoutModels.ItemModelo(1L, 1, 3, 10, BigDecimal.ZERO);
            apiClient.post("/api/modelos", new WorkoutModels.NovoModelo(identificacao, List.of(item)), WorkoutModels.TreinoResposta.class);
            inputIdentificacao.clear();
            lblErro.setText("");
            carregar();
        } catch (Exception erro) { mostrarErro("Não foi possível criar o modelo."); }
    }

    private void iniciar(Long id) {
        try {
            WorkoutModels.TreinoResposta execucao = apiClient.post("/api/modelos/" + id + "/iniciar", null, WorkoutModels.TreinoResposta.class);
            workoutSession.definir(execucao);
            navigationService.navegar("/fxml/active_workout_view.fxml");
        } catch (Exception erro) { mostrarErro("Não foi possível iniciar o modelo."); }
    }

    private void mostrarErro(String mensagem) { lblErro.setText(mensagem); }
    @FXML private void handleNavInicio() { navigationService.navegar("/fxml/dashboard_home_view.fxml"); }
    @FXML private void handleNavMetricas() { navigationService.navegar("/fxml/metrics_hub_view.fxml"); }
    @FXML private void handleContinuarTreino() {
        if (workoutSession.obter() == null) {
            mostrarErro("Nenhum treino ativo para continuar.");
            return;
        }
        navigationService.navegar("/fxml/active_workout_view.fxml");
    }
}
