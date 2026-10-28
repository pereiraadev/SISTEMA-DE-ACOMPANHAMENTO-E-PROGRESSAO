package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Controller;

import java.io.InputStream;

@Controller
public class MetricsHubController {

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

    // 1. ABA GRUPOS MUSCULARES
    private void carregarTabGrupos() {
        containerCards.getChildren().clear();

        adicionarCardGrupo("/icons/Frame (1).png", "Peitoral", "Volume semanal: 8.450 kg", "+12% vol", true, () -> abrirDetalhePeitoral()); //[cite: 37, 41]
        adicionarCardGrupo("/icons/Frame (2).png", "Costas", "Volume semanal: 7.210 kg", "+8% vol", true, null); //[cite: 36, 41]
        adicionarCardGrupo("/icons/Frame (3).png", "Pernas", "Volume semanal: 12.300 kg", "-3% vol", false, null); //[cite: 35, 41]
        adicionarCardGrupo("/icons/Frame (4).png", "Ombros", "Volume semanal: 4.120 kg", "+5% vol", true, null); //[cite: 34, 41]
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

        adicionarCardExercicio("Supino Reto", "Peitoral", "PR: 100 kg", () -> abrirExercicioEspecífico("supino_reto")); //[cite: 39]
        adicionarCardExercicio("Agachamento Livre", "Pernas", "PR: 180 kg", () -> abrirExercicioEspecífico("agachamento")); //[cite: 39]
        adicionarCardExercicio("Levantamento Terra", "Costas", "PR: 160 kg", null); //[cite: 39]
        adicionarCardExercicio("Desenvolvimento", "Ombros", "PR: 60 kg", null); //[cite: 39]
        adicionarCardExercicio("Rosca Direta", "Bíceps", "PR: 40 kg", null); //[cite: 39]
        adicionarCardExercicio("Tríceps Corda", "Tríceps", "PR: 35 kg", null); //[cite: 39]
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

        adicionarCardTreino("Treino A — Peito e Tríceps", "Último: 18 Set 2025", "85%", 24, null); //[cite: 38]
        adicionarCardTreino("Treino B — Costas e Bíceps", "Último: 17 Set 2025", "78%", 22, null); //[cite: 38]
        adicionarCardTreino("Treino C — Pernas e Ombros", "Último: 16 Set 2025", "92%", 26, null); //[cite: 38]
        adicionarCardTreino("Treino D — Full Body", "Último: 14 Set 2025", "65%", 12, null); //[cite: 38]
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

    private void abrirDetalhePeitoral() {
        System.out.println("Navegando para o Detalhe do Peitoral...");
    }

    private void abrirExercicioEspecífico(String id) {
        System.out.println("Navegando para Detalhes do Exercício: " + id);
    }

    @FXML private void handleNavInicio() { System.out.println("Navegar para Home"); }
    @FXML private void handleNavTreinos() { System.out.println("Navegar para Treinos"); }
}