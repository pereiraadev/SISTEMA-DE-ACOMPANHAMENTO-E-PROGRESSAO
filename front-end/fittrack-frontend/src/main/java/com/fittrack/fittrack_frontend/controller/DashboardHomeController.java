package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.dto.EstatisticasGeraisDTO;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.ItemTreinoDTO;
import com.fittrack.fittrack_frontend.dto.TreinoAtivoDTO;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class DashboardHomeController {

    @FXML private Label lblNomeUsuario;
    @FXML private Label lblTotalTreinos;
    @FXML private Label lblTotalExercicios;
    @FXML private Label lblTotalSeries;
    @FXML private Label lblVolumeTotal;

    @FXML private Button btnIniciarTreino;
    @FXML private HBox containerExerciciosMaisRealizados;

    @FXML
    public void initialize() {
        if (containerExerciciosMaisRealizados != null) {
            containerExerciciosMaisRealizados.getChildren().clear();
        }
    }

    /**
     * Injeta e exibe os dados estatísticos reais recebidos da API Spring Boot.
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

            Label lblInfo = new Label(ex.grupoMuscular() != null ? ex.grupoMuscular() : "Geral");
            lblInfo.setStyle("-fx-text-fill: #A0A0A0; -fx-font-size: 12px;");

            card.getChildren().addAll(lblNome, lblInfo);

            card.setOnMouseClicked(e -> handleVerDetalhesExercicio(ex));

            containerExerciciosMaisRealizados.getChildren().add(card);
        }
    }

    @FXML
    private void handleIniciarTreinoDoDia() {
        Button btnOrigem = (btnIniciarTreino != null) ? btnIniciarTreino : new Button();

        // Navega até a tela de Treino Ativo e obtém o Controller de destino para injetar o treino
        ActiveWorkoutController controller = NavigationManager.navegarComController(btnOrigem, "/fxml/active_workout.fxml");

        if (controller != null) {
            // Exemplo de payload inicial de sessão para renderização limpa
            ItemTreinoDTO primeiroItem = new ItemTreinoDTO(
                1L, 101L, "Supino Reto", "Peito", 1, 4, 10, 80.0, false, "4×10 - 80kg", List.of()
            );

            TreinoAtivoDTO treinoAtivo = new TreinoAtivoDTO(
                100L, 1L, "Treino A - Peito e Tríceps", 0, List.of(primeiroItem)
            );

            controller.carregarDadosTreino(treinoAtivo, primeiroItem);
        }
    }

    @FXML
    private void handleVerTodosExercicios() {
        NavigationManager.navegarPara(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
    }

    private void handleVerDetalhesExercicio(ExercicioDTO exercicio) {
        if (exercicio != null) {
            System.out.println("Abrindo detalhes do exercício ID: " + exercicio.id());
            NavigationManager.navegarPara(getNoReferencia(), "/fxml/exercise_details_view.fxml");
        }
    }

    @FXML
    private void handleNavInicio() {
        // Já se encontra na tela inicial (Home)
    }

    @FXML
    private void handleNavTreinos() {
        NavigationManager.navegarPara(getNoReferencia(), "/fxml/metrics_hub_view.fxml");
    }

    @FXML
    private void handleNavMetricas() {
        NavigationManager.navegarPara(getNoReferencia(), "/fxml/group_detail_view.fxml");
    }

    private Button getNoReferencia() {
        return (btnIniciarTreino != null) ? btnIniciarTreino : new Button();
    }
}
