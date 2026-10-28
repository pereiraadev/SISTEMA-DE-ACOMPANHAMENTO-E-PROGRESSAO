package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.client.PraticanteApiClient;
import com.fittrack.fittrack_frontend.client.TreinoApiClient;
import com.fittrack.fittrack_frontend.dto.DesempenhoGrupoDTO;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.GrupoMuscularEnum;
import com.fittrack.fittrack_frontend.dto.MetricaTreinoDTO;
import com.fittrack.fittrack_frontend.util.FxAsync;
import com.fittrack.fittrack_frontend.util.NavigationManager;

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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Scope("prototype")
public class MetricsHubController {

    @FXML private Button btnTabGrupos;
    @FXML private Button btnTabExercicios;
    @FXML private Button btnTabTreinos;

    @FXML private HBox boxBusca;
    @FXML private TextField inputBusca;
    @FXML private VBox containerCards;
    @FXML private HBox boxRodapeTotal;
    @FXML private Label lblTotalRealizados;

    private enum AbaAtiva { GRUPOS, EXERCICIOS, TREINOS }
    private AbaAtiva abaAtivaAtual = AbaAtiva.GRUPOS;

    private final List<DesempenhoGrupoDTO> listaGrupos = new ArrayList<>();
    private final List<ExercicioDTO> listaExercicios = new ArrayList<>();
    private final List<MetricaTreinoDTO> listaTreinos = new ArrayList<>();

    private final PraticanteApiClient praticanteApiClient;
    private final TreinoApiClient treinoApiClient;

    public MetricsHubController(PraticanteApiClient praticanteApiClient, TreinoApiClient treinoApiClient) {
        this.praticanteApiClient = praticanteApiClient;
        this.treinoApiClient = treinoApiClient;
    }

    /** Dados das três abas, buscados de uma só vez fora da thread da interface. */
    private record DadosMetricas(List<DesempenhoGrupoDTO> grupos,
                                 List<ExercicioDTO> exercicios,
                                 List<MetricaTreinoDTO> treinos) {}

    @FXML
    public void initialize() {
        if (inputBusca != null) {
            inputBusca.textProperty().addListener((obs, oldVal, newVal) -> {
                if (abaAtivaAtual == AbaAtiva.EXERCICIOS) {
                    filtrarERenderizarExercicios(newVal);
                }
            });
        }

        if (containerCards != null) {
            containerCards.getChildren().clear();
            exibirMensagemVazia("Carregando métricas...");
        }

        Long praticanteId = MainController.getUsuarioLogadoId();
        if (praticanteId == null) {
            carregarMetricas(List.of(), List.of(), List.of());
            return;
        }

        FxAsync.executar(
                () -> new DadosMetricas(
                        praticanteApiClient.listarDesempenhoGrupos(praticanteId),
                        praticanteApiClient.listarExercicios(praticanteId),
                        treinoApiClient.listarMetricasTreinos(praticanteId)),
                dados -> carregarMetricas(dados.grupos(), dados.exercicios(), dados.treinos()),
                erro -> {
                    carregarMetricas(List.of(), List.of(), List.of());
                    FxAsync.exibirErro("Não foi possível carregar as métricas", erro);
                });
    }

    /** Permite que outras telas abram o hub já na aba desejada. */
    public void selecionarAbaExercicios() { handleSelecionarTabExercicios(); }

    public void selecionarAbaTreinos() { handleSelecionarTabTreinos(); }

    /**
     * Injeta e atualiza os dados dinâmicos de métricas vindos da API Spring Boot.
     */
    public void carregarMetricas(List<DesempenhoGrupoDTO> grupos,
                                 List<ExercicioDTO> exercicios,
                                 List<MetricaTreinoDTO> treinos) {
        this.listaGrupos.clear();
        if (grupos != null) this.listaGrupos.addAll(grupos);

        this.listaExercicios.clear();
        if (exercicios != null) this.listaExercicios.addAll(exercicios);

        this.listaTreinos.clear();
        if (treinos != null) this.listaTreinos.addAll(treinos);

        switch (abaAtivaAtual) {
            case GRUPOS -> carregarTabGrupos();
            case EXERCICIOS -> carregarTabExercicios();
            case TREINOS -> carregarTabTreinos();
        }
    }

    @FXML
    private void handleSelecionarTabGrupos() {
        abaAtivaAtual = AbaAtiva.GRUPOS;
        resetTabStyles();
        if (btnTabGrupos != null) btnTabGrupos.getStyleClass().setAll("button", "tab-button-active");
        carregarTabGrupos();
    }

    @FXML
    private void handleSelecionarTabExercicios() {
        abaAtivaAtual = AbaAtiva.EXERCICIOS;
        resetTabStyles();
        if (btnTabExercicios != null) btnTabExercicios.getStyleClass().setAll("button", "tab-button-active");
        carregarTabExercicios();
    }

    @FXML
    private void handleSelecionarTabTreinos() {
        abaAtivaAtual = AbaAtiva.TREINOS;
        resetTabStyles();
        if (btnTabTreinos != null) btnTabTreinos.getStyleClass().setAll("button", "tab-button-active");
        carregarTabTreinos();
    }

    private void resetTabStyles() {
        for (Button aba : new Button[] { btnTabGrupos, btnTabExercicios, btnTabTreinos }) {
            if (aba != null) aba.getStyleClass().setAll("button", "tab-button");
        }

        if (boxBusca != null) {
            boxBusca.setVisible(false);
            boxBusca.setManaged(false);
        }
        if (boxRodapeTotal != null) {
            boxRodapeTotal.setVisible(false);
            boxRodapeTotal.setManaged(false);
        }
    }

    // 1. ABA GRUPOS MUSCULARES
    private void carregarTabGrupos() {
        if (containerCards == null) return;
        containerCards.getChildren().clear();

        if (listaGrupos.isEmpty()) {
            exibirMensagemVazia("Nenhum grupo muscular registrado.");
            return;
        }

        for (DesempenhoGrupoDTO grupo : listaGrupos) {
            String sub = String.format("Volume semanal: %,.0f kg", grupo.volumeSemanal());
            String iconPath = obterIconeGrupo(grupo.grupoMuscular());
            
            adicionarCardGrupo(
                iconPath,
                GrupoMuscularEnum.rotuloDe(grupo.grupoMuscular()),
                sub,
                grupo.percentualVariacao() != null ? grupo.percentualVariacao() : "0%",
                grupo.isAumento(),
                () -> abrirDetalheGrupo(grupo)
            );
        }
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
        if (boxBusca != null) {
            boxBusca.setVisible(true);
            boxBusca.setManaged(true);
        }
        String termo = (inputBusca != null && inputBusca.getText() != null) ? inputBusca.getText() : "";
        filtrarERenderizarExercicios(termo);
    }

    private void filtrarERenderizarExercicios(String termo) {
        if (containerCards == null) return;
        containerCards.getChildren().clear();

        List<ExercicioDTO> filtrados = listaExercicios;
        if (termo != null && !termo.trim().isEmpty()) {
            String t = termo.trim().toLowerCase();
            filtrados = listaExercicios.stream()
                    .filter(e -> (e.nome() != null && e.nome().toLowerCase().contains(t)) ||
                                 GrupoMuscularEnum.rotuloDe(e.grupoMuscular()).toLowerCase().contains(t))
                    .collect(Collectors.toList());
        }

        if (filtrados.isEmpty()) {
            exibirMensagemVazia("Nenhum exercício encontrado.");
            return;
        }

        for (ExercicioDTO ex : filtrados) {
            String prText = ex.recordeCarga() != null ? String.format("PR: %.0f kg", ex.recordeCarga()) : "Sem PR";
            adicionarCardExercicio(
                ex.nome(),
                GrupoMuscularEnum.rotuloDe(ex.grupoMuscular()),
                prText,
                () -> abrirDetalheExercicio(ex)
            );
        }
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
        if (boxRodapeTotal != null) {
            boxRodapeTotal.setVisible(true);
            boxRodapeTotal.setManaged(true);
        }
        if (containerCards == null) return;
        containerCards.getChildren().clear();

        if (listaTreinos.isEmpty()) {
            exibirMensagemVazia("Nenhum treino registrado.");
            if (lblTotalRealizados != null) lblTotalRealizados.setText("Total: 0 treinos realizados");
            return;
        }

        int somaSessoes = 0;
        for (MetricaTreinoDTO t : listaTreinos) {
            somaSessoes += t.totalSessoes();
            String sub = "Último: " + (t.ultimaSessaoData() != null ? t.ultimaSessaoData() : "—");
            String consistenciaStr = t.consistenciaPercentual() + "%";
            
            adicionarCardTreino(
                t.identificacao(),
                sub,
                consistenciaStr,
                t.totalSessoes(),
                null // não há tela de histórico por treino no conjunto de FXMLs atual
            );
        }

        if (lblTotalRealizados != null) {
            lblTotalRealizados.setText("Total: " + somaSessoes + " treinos realizados");
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
        int pct = 0;
        try {
            pct = Integer.parseInt(consistencia.replace("%", "").trim());
        } catch (NumberFormatException ignored) {}
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

    private String obterIconeGrupo(String grupo) {
        return GrupoMuscularEnum.from(grupo).map(GrupoMuscularEnum::icone).orElse("/icons/Frame (1).png");
    }

    private void exibirMensagemVazia(String mensagem) {
        Label lbl = new Label(mensagem);
        lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        containerCards.getChildren().add(lbl);
    }

    private void abrirDetalheGrupo(DesempenhoGrupoDTO grupo) {
        GroupDetailController controller = NavigationManager.navegarComController(containerCards, "/fxml/group_detail_view.fxml");
        if (controller != null && grupo != null) {
            controller.carregarGrupo(grupo);
        }
    }

    private void abrirDetalheExercicio(ExercicioDTO ex) {
        ExerciseDetailsController controller = NavigationManager.navegarComController(containerCards, "/fxml/exercise_details_view.fxml");
        if (controller != null && ex != null) {
            controller.carregarDetalhesExercicio(ex);
        }
    }

    @FXML 
    private void handleNavInicio() { 
        if (containerCards != null) {
            NavigationManager.navegarPara(containerCards, "/fxml/dashboard_home_view.fxml"); 
        }
    }

    @FXML 
    private void handleNavTreinos() { 
        handleSelecionarTabTreinos();
    }
}
