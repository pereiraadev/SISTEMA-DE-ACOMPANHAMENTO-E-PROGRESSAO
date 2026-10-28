package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fittrack.fittrack_frontend.service.ApiClient;
import com.fittrack.fittrack_frontend.service.NavigationService;
import com.fittrack.fittrack_frontend.service.WorkoutModels;

import java.io.InputStream;
import java.util.List;

@Controller
public class MetricsHubController {
    private final ApiClient apiClient;
    private final NavigationService navigationService;
    private final com.fittrack.fittrack_frontend.service.ExerciseSelection exerciseSelection;
    private final com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession;

    public MetricsHubController(ApiClient apiClient, NavigationService navigationService,
                                 com.fittrack.fittrack_frontend.service.ExerciseSelection exerciseSelection,
                                 com.fittrack.fittrack_frontend.service.WorkoutSession workoutSession) {
        this.apiClient = apiClient;
        this.navigationService = navigationService;
        this.exerciseSelection = exerciseSelection;
        this.workoutSession = workoutSession;
    }

    @FXML private Button btnTabGrupos;
    @FXML private Button btnTabExercicios;
    @FXML private Button btnTabTreinos;

    @FXML private HBox boxBusca;
    @FXML private TextField inputBusca;
    @FXML private VBox containerCards;
    @FXML private HBox boxRodapeTotal;
    @FXML private Label lblTotalRealizados;

    @FXML
    public void initialize() {
        carregarTabGrupos();
    }

    @FXML
    private void handleSelecionarTabGrupos() {
        resetTabStyles();
        btnTabGrupos.getStyleClass().add("tab-button-active");
        carregarTabGrupos();
    }

    @FXML
    private void handleSelecionarTabExercicios() {
        resetTabStyles();
        btnTabExercicios.getStyleClass().add("tab-button-active");
        carregarTabExercicios();
    }

    @FXML
    private void handleSelecionarTabTreinos() {
        resetTabStyles();
        btnTabTreinos.getStyleClass().add("tab-button-active");
        carregarTabTreinos();
    }

    private void resetTabStyles() {
        btnTabGrupos.getStyleClass().removeAll("tab-button-active");
        btnTabExercicios.getStyleClass().removeAll("tab-button-active");
        btnTabTreinos.getStyleClass().removeAll("tab-button-active");

        btnTabGrupos.getStyleClass().add("tab-button");
        btnTabExercicios.getStyleClass().add("tab-button");
        btnTabTreinos.getStyleClass().add("tab-button");

        boxBusca.setVisible(false);
        boxBusca.setManaged(false);
        boxRodapeTotal.setVisible(false);
        boxRodapeTotal.setManaged(false);
    }

    private void carregarTabGrupos() {
        containerCards.getChildren().clear();
        try {
            var dashboard = apiClient.get("/api/dashboard", com.fittrack.fittrack_frontend.service.DashboardModels.DashboardResposta.class);
            if (dashboard.volumePorGrupo() == null || dashboard.volumePorGrupo().isEmpty()) {
                adicionarEstadoVazio("Nenhum grupo muscular com dados.");
                return;
            }
            dashboard.volumePorGrupo().forEach((grupo, volume) -> adicionarCardGrupo(null, grupo,
                    String.format("Volume registrado: %,.0f kg", volume), "", true,
                    () -> navigationService.navegar("/fxml/group_detail_view.fxml")));
        } catch (Exception erro) {
            adicionarEstadoVazio("Não foi possível carregar as métricas.");
        }
    }

    private void adicionarEstadoVazio(String texto) {
        Label estado = new Label(texto);
        estado.getStyleClass().add("card-subtitle");
        containerCards.getChildren().add(estado);
    }

    private void adicionarCardGrupo(String iconPath, String titulo, String sub, String varVol, boolean positivo, Runnable onAction) {
        HBox card = new HBox(12.0);
        card.getStyleClass().add("metric-card-row");
        card.setAlignment(Pos.CENTER_LEFT);
        if (onAction != null) card.setOnMouseClicked(e -> onAction.run());

        ImageView icon = carregarIconeCircular(iconPath);

        VBox info = new VBox(2.0);
        Label lblT = new Label(titulo);
        lblT.getStyleClass().add("card-title");
        Label lblS = new Label(sub);
        lblS.getStyleClass().add("card-subtitle");
        info.getChildren().addAll(lblT, lblS);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label badge = new Label(varVol);
        badge.getStyleClass().add(positivo ? "badge-vol-positive" : "badge-vol-negative");

        Label setinha = new Label("›");
        setinha.setStyle("-fx-text-fill: #666666; -fx-font-size: 18px; -fx-font-weight: bold;");

        card.getChildren().addAll(icon, info, badge, setinha);
        containerCards.getChildren().add(card);
    }

    // 2. ABA EXERCÍCIOS
    private void carregarTabExercicios() {
        boxBusca.setVisible(true);
        boxBusca.setManaged(true);
        containerCards.getChildren().clear();
        try {
            List<WorkoutModels.ExercicioResposta> exercicios = apiClient.getList("/api/exercicios", new TypeReference<>() {});
            for (WorkoutModels.ExercicioResposta exercicio : exercicios) {
                adicionarCardExercicio(exercicio.nome(), exercicio.grupoMuscular(),
                        "PR: " + (exercicio.recordeCarga() == null ? "—" : exercicio.recordeCarga() + " kg"),
                        () -> abrirExercicioEspecífico(String.valueOf(exercicio.id())));
            }
        } catch (Exception erro) {
            adicionarEstadoVazio("Não foi possível carregar os exercícios.");
        }
        if (containerCards.getChildren().isEmpty()) adicionarEstadoVazio("Nenhum exercício encontrado.");
    }

    private void adicionarCardExercicio(String titulo, String sub, String prText, Runnable onAction) {
        HBox card = new HBox(12.0);
        card.getStyleClass().add("metric-card-row");
        card.setAlignment(Pos.CENTER_LEFT);
        if (onAction != null) card.setOnMouseClicked(e -> onAction.run());

        VBox info = new VBox(2.0);
        Label lblT = new Label(titulo);
        lblT.getStyleClass().add("card-title");
        Label lblS = new Label(sub);
        lblS.getStyleClass().add("card-subtitle");
        info.getChildren().addAll(lblT, lblS);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label pr = new Label(prText);
        pr.getStyleClass().add("badge-pr-highlight");

        Label setinha = new Label("›");
        setinha.setStyle("-fx-text-fill: #666666; -fx-font-size: 18px; -fx-font-weight: bold;");

        card.getChildren().addAll(info, pr, setinha);
        containerCards.getChildren().add(card);
    }

    // 3. ABA TREINOS
    private void carregarTabTreinos() {
        boxRodapeTotal.setVisible(true);
        boxRodapeTotal.setManaged(true);
        containerCards.getChildren().clear();

        try {
            List<WorkoutModels.TreinoResposta> treinos = apiClient.getList("/api/modelos", new TypeReference<>() {});
            for (WorkoutModels.TreinoResposta treino : treinos) {
                adicionarCardTreino(treino.identificacao(), "Modelo de treino", "—", 0,
                        () -> iniciarTreino(treino.id()));
            }
            lblTotalRealizados.setText(treinos.size() + " modelos disponíveis");
            if (treinos.isEmpty()) adicionarEstadoVazio("Nenhum modelo de treino disponível.");
        } catch (Exception erro) {
            adicionarEstadoVazio("Não foi possível carregar os treinos.");
        }
    }

    private void adicionarCardTreino(String titulo, String sub, String consistencia, int sessoes, Runnable onAction) {
        VBox card = new VBox(10.0);
        card.getStyleClass().add("metric-card-row");
        if (onAction != null) card.setOnMouseClicked(e -> onAction.run());

        HBox topo = new HBox();
        topo.setAlignment(Pos.CENTER_LEFT);
        VBox info = new VBox(2.0);
        Label lblT = new Label(titulo);
        lblT.getStyleClass().add("card-title");
        Label lblS = new Label(sub);
        lblS.getStyleClass().add("card-subtitle");
        info.getChildren().addAll(lblT, lblS);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label setinha = new Label("›");
        setinha.setStyle("-fx-text-fill: #666666; -fx-font-size: 18px; -fx-font-weight: bold;");
        topo.getChildren().addAll(info, spacer, setinha);

        // Linha com barra de consistência e sessões
        HBox baixo = new HBox();
        baixo.setAlignment(Pos.CENTER_LEFT);

        VBox barBox = new VBox(4.0);
        HBox lblBox = new HBox(4.0);
        Label lblC = new Label("Consistência: ");
        lblC.setStyle("-fx-text-fill: #888888; -fx-font-size: 11px;");
        Label lblV = new Label(consistencia);
        lblV.setStyle("-fx-text-fill: #43D61B; -fx-font-size: 11px; -fx-font-weight: bold;");
        lblBox.getChildren().addAll(lblC, lblV);

        StackPane progressBg = new StackPane();
        progressBg.getStyleClass().add("progress-bar-bg");
        progressBg.setPrefWidth(160.0);
        progressBg.setAlignment(Pos.CENTER_LEFT);

        Region progressFill = new Region();
        progressFill.getStyleClass().add("progress-bar-fill");
        int pct = Integer.parseInt(consistencia.replace("%", ""));
        progressFill.setPrefWidth(160.0 * (pct / 100.0));

        progressBg.getChildren().add(progressFill);
        barBox.getChildren().addAll(lblBox, progressBg);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        VBox sessBox = new VBox(0.0);
        sessBox.setAlignment(Pos.CENTER_RIGHT);
        Label lblSess = new Label(String.valueOf(sessoes));
        lblSess.setStyle("-fx-text-fill: #FFFFFF; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label lblSessLabel = new Label("sessões");
        lblSessLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");
        sessBox.getChildren().addAll(lblSess, lblSessLabel);

        baixo.getChildren().addAll(barBox, spacer2, sessBox);

        card.getChildren().addAll(topo, baixo);
        containerCards.getChildren().add(card);
    }

    private ImageView carregarIconeCircular(String path) {
        ImageView img = new ImageView();
        img.setFitWidth(36.0);
        img.setFitHeight(36.0);
        InputStream stream = getClass().getResourceAsStream(path);
        if (stream != null) {
            img.setImage(new Image(stream));
        }
        return img;
    }

    private void iniciarTreino(Long modeloId) {
        try {
            var execucao = apiClient.post("/api/modelos/" + modeloId + "/iniciar", null, WorkoutModels.TreinoResposta.class);
            workoutSession.definir(execucao);
            navigationService.navegar("/fxml/active_workout_view.fxml");
        } catch (Exception erro) {
            adicionarEstadoVazio("Não foi possível iniciar o treino.");
        }
    }

    private void abrirExercicioEspecífico(String id) {
        exerciseSelection.selecionar(Long.valueOf(id));
        navigationService.navegar("/fxml/exercise_details_view.fxml");
    }

    @FXML private void handleNavInicio() { navigationService.navegar("/fxml/dashboard_home_view.fxml"); }
    @FXML private void handleNavTreinos() { navigationService.navegar("/fxml/active_workout_view.fxml"); }
}