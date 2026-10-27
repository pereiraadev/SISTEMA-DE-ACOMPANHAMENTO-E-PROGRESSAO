package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardHomeController {

    @FXML
    private void handleIniciarTreinoDoDia() {
        System.out.println("Navegando para: Tela de Treino Ativo...");
        // Exemplo: NavigationService.navigate("/fxml/treino_ativo_view.fxml");
    }

    @FXML
    private void handleVerTodosExercicios() {
        System.out.println("Navegando para: Métricas de Exercícios...");
        // Exemplo: NavigationService.navigate("/fxml/metricas_exercicios_view.fxml");
    }

    @FXML
    private void handleNavInicio() {
        // Já está na tela Home
    }

    @FXML
    private void handleNavTreinos() {
        System.out.println("Navegando para: Métricas / Lista de Treinos...");
        // Exemplo: NavigationService.navigate("/fxml/metricas_treino_view.fxml");
    }

    @FXML
    private void handleNavMetricas() {
        System.out.println("Navegando para: Métricas Grupos Musculares...");
        // Exemplo: NavigationService.navigate("/fxml/metricas_grupos_view.fxml");
    }
}
