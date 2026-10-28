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

/**
 * Controla o hub de métricas em {@code metrics_hub_view.fxml}, alternando entre grupos,
 * exercícios e treinos; cada aba recompõe os nós exibidos conforme os dados da API.
 */
@Controller
@Scope("prototype")
public class MetricsHubController {

    /** Aba que apresenta o desempenho por grupo muscular. */
    @FXML private Button btnTabGrupos;
    /** Aba que apresenta e permite filtrar o catálogo de exercícios. */
    @FXML private Button btnTabExercicios;
    /** Aba que apresenta consistência e recorrência das fichas. */
    @FXML private Button btnTabTreinos;

    /** Contêiner de busca exibido somente na aba de exercícios. */
    @FXML private HBox boxBusca;
    /** Texto cujo binding de mudança dispara o filtro dos cards de exercício. */
    @FXML private TextField inputBusca;
    /** Área central preenchida dinamicamente com os cards de cada aba. */
    @FXML private VBox containerCards;
    /** Rodapé de total exibido na aba de treinos. */
    @FXML private HBox boxRodapeTotal;
    /** Soma das sessões das fichas exibidas. */
    @FXML private Label lblTotalRealizados;

    /** Aba atualmente selecionada, usada para escolher a rotina de renderização. */
    private enum AbaAtiva { GRUPOS, EXERCICIOS, TREINOS }
    private AbaAtiva abaAtivaAtual = AbaAtiva.GRUPOS;

    /** Estado local dos dados de desempenho retornados para grupos. */
    private final List<DesempenhoGrupoDTO> listaGrupos = new ArrayList<>();
    /** Catálogo local usado para filtrar sem repetir chamadas à API. */
    private final List<ExercicioDTO> listaExercicios = new ArrayList<>();
    /** Métricas locais de consistência por ficha. */
    private final List<MetricaTreinoDTO> listaTreinos = new ArrayList<>();

    /** Cliente REST para métricas e catálogo do praticante. */
    private final PraticanteApiClient praticanteApiClient;
    /** Cliente REST para métricas de ficha de treino. */
    private final TreinoApiClient treinoApiClient;

    /**
     * Injeta os clientes usados para preencher as três abas.
     *
     * @param praticanteApiClient cliente de dados de praticante
     * @param treinoApiClient cliente de dados de treino
     */
    public MetricsHubController(PraticanteApiClient praticanteApiClient, TreinoApiClient treinoApiClient) {
        this.praticanteApiClient = praticanteApiClient;
        this.treinoApiClient = treinoApiClient;
    }

    /** Resposta agrupada para reduzir a coordenação entre chamadas assíncronas independentes. */
    private record DadosMetricas(
            /** Indicadores por grupo muscular. */
            List<DesempenhoGrupoDTO> grupos,
            /** Catálogo para a aba de exercícios. */
            List<ExercicioDTO> exercicios,
            /** Métricas de consistência de cada ficha. */
            List<MetricaTreinoDTO> treinos) {}

    /** Prepara a busca reativa e carrega os dados das abas fora da thread da interface. */
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

    /**
     * Seleciona a aba de exercícios, por exemplo após uma ação de navegação do dashboard.
     */
    public void selecionarAbaExercicios() { handleSelecionarTabExercicios(); }

    /**
     * Seleciona a aba de treinos, reutilizando a mesma lógica dos botões do FXML.
     */
    public void selecionarAbaTreinos() { handleSelecionarTabTreinos(); }

    /**
     * Substitui os dados locais e renderiza a aba atualmente selecionada.
     *
     * @param grupos métricas por grupo muscular
     * @param exercicios catálogo de exercícios disponíveis
     * @param treinos métricas de consistência por ficha
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
        // Evento de clique: atualiza o estado da aba e reconstrói os controles visíveis.
        abaAtivaAtual = AbaAtiva.GRUPOS;
        resetTabStyles();
        if (btnTabGrupos != null) btnTabGrupos.getStyleClass().setAll("button", "tab-button-active");
        carregarTabGrupos();
    }

    @FXML
    private void handleSelecionarTabExercicios() {
        // A aba habilita busca por texto e reutiliza a lista em memória para filtrar em cada digitação.
        abaAtivaAtual = AbaAtiva.EXERCICIOS;
        resetTabStyles();
        if (btnTabExercicios != null) btnTabExercicios.getStyleClass().setAll("button", "tab-button-active");
        carregarTabExercicios();
    }

    @FXML
    private void handleSelecionarTabTreinos() {
        // A aba exibe o rodapé com a soma de sessões além dos cartões individuais.
        abaAtivaAtual = AbaAtiva.TREINOS;
        resetTabStyles();
        if (btnTabTreinos != null) btnTabTreinos.getStyleClass().setAll("button", "tab-button-active");
        carregarTabTreinos();
    }

    /** Remove o estilo ativo anterior e oculta controles específicos de outras abas. */
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

    // Os cards são construídos em tempo de execução para representar apenas os dados da API.
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

    /** Monta card clicável de grupo, incluindo ícone, volume semanal e indicação de variação. */
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

    // A área de busca acompanha a aba ativa e o listener de texto aciona a filtragem.
    private void carregarTabExercicios() {
        if (boxBusca != null) {
            boxBusca.setVisible(true);
            boxBusca.setManaged(true);
        }
        String termo = (inputBusca != null && inputBusca.getText() != null) ? inputBusca.getText() : "";
        filtrarERenderizarExercicios(termo);
    }

    /** Filtra por nome ou rótulo muscular e substitui os cards visíveis com o resultado. */
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

    /** Cria um card de exercício e associa seu clique à abertura do respectivo histórico. */
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

    // A aba agrega consistência e número de sessões para fichas já utilizadas.
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

    /** Monta o card de ficha, desenhando a barra proporcional ao percentual de consistência. */
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

    /** Carrega um recurso de ícone para o card, dimensionado de forma consistente. */
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

    /** Resolve o ícone associado ao grupo ou usa o ícone padrão quando não houver correspondência. */
    private String obterIconeGrupo(String grupo) {
        return GrupoMuscularEnum.from(grupo).map(GrupoMuscularEnum::icone).orElse("/icons/Frame (1).png");
    }

    /** Insere um rótulo de estado vazio no mesmo contêiner usado pelos cartões. */
    private void exibirMensagemVazia(String mensagem) {
        Label lbl = new Label(mensagem);
        lbl.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        containerCards.getChildren().add(lbl);
    }

    /** Navega para a tela do grupo e entrega o DTO selecionado ao controller recém-criado. */
    private void abrirDetalheGrupo(DesempenhoGrupoDTO grupo) {
        GroupDetailController controller = NavigationManager.navegarComController(containerCards, "/fxml/group_detail_view.fxml");
        if (controller != null && grupo != null) {
            controller.carregarGrupo(grupo);
        }
    }

    /** Navega ao histórico do exercício e encaminha os dados já disponíveis no card. */
    private void abrirDetalheExercicio(ExercicioDTO ex) {
        ExerciseDetailsController controller = NavigationManager.navegarComController(containerCards, "/fxml/exercise_details_view.fxml");
        if (controller != null && ex != null) {
            controller.carregarDetalhesExercicio(ex);
        }
    }

    @FXML 
    private void handleNavInicio() { 
        // O clique na navegação retorna ao dashboard inicial.
        if (containerCards != null) {
            NavigationManager.navegarPara(containerCards, "/fxml/dashboard_home_view.fxml"); 
        }
    }

    @FXML 
    private void handleNavTreinos() { 
        // Mantém a tela e seleciona a aba Treinos em vez de carregar outra Scene.
        handleSelecionarTabTreinos();
    }
}
