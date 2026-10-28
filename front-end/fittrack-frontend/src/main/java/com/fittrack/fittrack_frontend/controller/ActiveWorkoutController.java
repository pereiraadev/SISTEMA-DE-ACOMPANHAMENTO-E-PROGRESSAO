package com.fittrack.fittrack_frontend.controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fittrack.fittrack_frontend.client.TreinoApiClient;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.ItemTreinoDTO;
import com.fittrack.fittrack_frontend.dto.RegistroHistoricoDTO;
import com.fittrack.fittrack_frontend.dto.SerieExecutadaDTO;
import com.fittrack.fittrack_frontend.dto.TreinoAtivoDTO;
import com.fittrack.fittrack_frontend.util.FxAsync;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Tela de treino ativo. Toda a interface é derivada do {@link TreinoAtivoDTO} carregado:
 * o índice do {@link ItemTreinoDTO} corrente define o exercício, a referência prevista e a grade de séries.
 */
@Controller
@Scope("prototype") // uma instância nova a cada carregamento da tela (estado da sessão não vaza)
public class ActiveWorkoutController {

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML private Label txtNomeTreino;
    @FXML private Label txtNomeExercicio;
    @FXML private Label txtGrupoMuscular;
    @FXML private Label txtReferenciaAnterior;
    @FXML private Label txtCronometro;
    @FXML private Label txtProgressoSeries;
    @FXML private Label txtProgressoExercicios;
    @FXML private VBox containerSeries;
    @FXML private Label txtVolumeSerie;
    @FXML private Label txtVolumeExercicio;
    @FXML private Button btnExercicioAnterior;
    @FXML private Button btnProximoExercicio;

    @FXML private HBox boxNotificacao;
    @FXML private Label lblNotificacaoTexto;

    private final TreinoApiClient treinoApiClient;

    private TreinoAtivoDTO treinoAtivo;
    private List<ItemTreinoDTO> itens = List.of();
    /** Séries concluídas de cada item (mesma posição de {@link #itens}). */
    private final List<List<SerieExecutadaDTO>> seriesPorItem = new ArrayList<>();
    private int itemIndex = 0;

    private long segundosDecorridos = 0;
    private Timeline timeline;
    private boolean finalizando = false;

    private TextField currentInputCarga;
    private TextField currentInputReps;

    public ActiveWorkoutController(TreinoApiClient treinoApiClient) {
        this.treinoApiClient = treinoApiClient;
    }

    @FXML
    public void initialize() {
        if (containerSeries != null) containerSeries.getChildren().clear();
    }

    // ------------------------------------------------------------------
    // Carregamento do treino
    // ------------------------------------------------------------------

    /** Recebe o treino vindo da API (ex.: DashboardHomeController) e monta toda a tela. */
    public void carregarTreino(TreinoAtivoDTO treino) {
        this.treinoAtivo = treino;
        this.segundosDecorridos = treino != null ? treino.segundosDecorridos() : 0;

        this.itens = (treino == null || treino.itens() == null) ? List.of()
                : treino.itens().stream()
                    .sorted(Comparator.comparing(ItemTreinoDTO::ordem, Comparator.nullsLast(Comparator.naturalOrder())))
                    .toList();

        seriesPorItem.clear();
        for (ItemTreinoDTO item : itens) {
            List<SerieExecutadaDTO> concluidas = new ArrayList<>();
            if (item.seriesRealizadas() != null) {
                item.seriesRealizadas().stream().filter(SerieExecutadaDTO::concluida).forEach(concluidas::add);
            }
            seriesPorItem.add(concluidas);
        }

        // Retoma no primeiro exercício que ainda tem séries pendentes.
        itemIndex = 0;
        for (int i = 0; i < itens.size(); i++) {
            if (seriesPorItem.get(i).size() < valor(itens.get(i).seriesPrevistas())) {
                itemIndex = i;
                break;
            }
        }

        if (txtNomeTreino != null) {
            txtNomeTreino.setText(treino != null && treino.identificacao() != null ? treino.identificacao() : "Treino");
        }

        atualizarCronometro();
        iniciarCronometro();
        renderizarItemAtual();
    }

    // ------------------------------------------------------------------
    // Cronômetro
    // ------------------------------------------------------------------

    private void iniciarCronometro() {
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundosDecorridos++;
            atualizarCronometro();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void pararCronometro() {
        if (timeline != null) timeline.stop();
    }

    private void atualizarCronometro() {
        if (txtCronometro == null) return;
        long horas = segundosDecorridos / 3600;
        long minutos = (segundosDecorridos % 3600) / 60;
        long segs = segundosDecorridos % 60;
        txtCronometro.setText(horas > 0
                ? String.format("%02d:%02d:%02d", horas, minutos, segs)
                : String.format("%02d:%02d", minutos, segs));
    }

    // ------------------------------------------------------------------
    // Renderização do exercício atual
    // ------------------------------------------------------------------

    private void renderizarItemAtual() {
        boolean temItens = !itens.isEmpty();

        if (btnExercicioAnterior != null) btnExercicioAnterior.setDisable(!temItens || itemIndex <= 0);
        if (btnProximoExercicio != null) btnProximoExercicio.setDisable(!temItens || itemIndex >= itens.size() - 1);

        if (!temItens) {
            if (txtNomeExercicio != null) txtNomeExercicio.setText("Sem exercícios");
            if (txtGrupoMuscular != null) txtGrupoMuscular.setText("");
            if (txtReferenciaAnterior != null) txtReferenciaAnterior.setText("—");
            if (txtProgressoExercicios != null) txtProgressoExercicios.setText("Exercício 0 de 0");
            renderizarSeries();
            return;
        }

        ItemTreinoDTO item = itens.get(itemIndex);
        ExercicioDTO exercicio = item.exercicio();

        if (txtNomeExercicio != null) {
            txtNomeExercicio.setText(exercicio != null && exercicio.nome() != null ? exercicio.nome() : "Exercício");
        }
        if (txtGrupoMuscular != null) {
            txtGrupoMuscular.setText(exercicio != null && exercicio.grupoMuscular() != null ? exercicio.grupoMuscular() : "");
        }
        if (txtReferenciaAnterior != null) {
            txtReferenciaAnterior.setText(valor(item.seriesPrevistas()) + "x" + valor(item.repsPrevistas())
                    + " - " + formatarKg(item.cargaPrevista()) + "kg");
        }
        if (txtProgressoExercicios != null) {
            txtProgressoExercicios.setText("Exercício " + (itemIndex + 1) + " de " + itens.size());
        }

        renderizarSeries();
    }

    private void renderizarSeries() {
        if (containerSeries == null) return;
        containerSeries.getChildren().clear();
        currentInputCarga = null;
        currentInputReps = null;

        if (itens.isEmpty()) {
            atualizarResumo(0, 0, 0.0, 0.0);
            return;
        }

        ItemTreinoDTO item = itens.get(itemIndex);
        List<SerieExecutadaDTO> series = seriesPorItem.get(itemIndex);
        int previstas = valor(item.seriesPrevistas());
        int totalLinhas = Math.max(previstas, series.size());

        double volumeExercicio = 0.0;
        for (int i = 0; i < totalLinhas; i++) {
            int numero = i + 1;
            if (i < series.size()) {
                SerieExecutadaDTO serie = series.get(i);
                volumeExercicio += volumeDe(serie);
                containerSeries.getChildren().add(criarLinhaConcluida(numero, serie));
            } else if (i == series.size()) {
                containerSeries.getChildren().add(criarLinhaAtiva(numero, item));
            } else {
                containerSeries.getChildren().add(criarLinhaPendente(numero));
            }
        }

        double volumeUltimaSerie = series.isEmpty() ? 0.0 : volumeDe(series.get(series.size() - 1));
        atualizarResumo(series.size(), previstas, volumeUltimaSerie, volumeExercicio);
    }

    private void atualizarResumo(int concluidas, int previstas, double volumeSerie, double volumeExercicio) {
        if (txtProgressoSeries != null) txtProgressoSeries.setText(concluidas + " de " + previstas + " séries");
        if (txtVolumeSerie != null) txtVolumeSerie.setText(String.format("%.0f kg", volumeSerie));
        if (txtVolumeExercicio != null) txtVolumeExercicio.setText(String.format("%.0f kg", volumeExercicio));
    }

    private GridPane criarGradeLinha() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER_LEFT);

        ColumnConstraints colNum = new ColumnConstraints(); colNum.setPercentWidth(12.0);
        ColumnConstraints colCarga = new ColumnConstraints(); colCarga.setPercentWidth(43.0);
        ColumnConstraints colReps = new ColumnConstraints(); colReps.setPercentWidth(25.0);
        ColumnConstraints colLog = new ColumnConstraints(); colLog.setPercentWidth(20.0);
        colLog.setHalignment(HPos.RIGHT);

        grid.getColumnConstraints().addAll(colNum, colCarga, colReps, colLog);
        return grid;
    }

    private GridPane criarLinhaConcluida(int numero, SerieExecutadaDTO serie) {
        GridPane grid = criarGradeLinha();
        grid.getStyleClass().add("set-row");
        grid.setPrefHeight(45.0);

        Label lblNum = new Label(String.valueOf(numero));
        lblNum.getStyleClass().add("set-number");

        Label lblCarga = new Label(formatarKg(serie.cargaUtilizada()) + " kg");
        lblCarga.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

        Label lblReps = new Label(String.valueOf(valor(serie.repeticoesRealizadas())));
        lblReps.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

        grid.add(lblNum, 0, 0);
        grid.add(lblCarga, 1, 0);
        grid.add(lblReps, 2, 0);
        grid.add(criarIcone("/icons/check-circle.png"), 3, 0);
        return grid;
    }

    /** Linha ativa: expõe os TextFields editáveis (currentInputCarga e currentInputReps). */
    private GridPane criarLinhaAtiva(int numero, ItemTreinoDTO item) {
        GridPane grid = criarGradeLinha();
        grid.getStyleClass().add("set-row-active");
        grid.setPrefHeight(48.0);

        Label lblNum = new Label(String.valueOf(numero));
        lblNum.getStyleClass().add("set-number");
        lblNum.setStyle("-fx-text-fill: #43D61B; -fx-font-weight: bold;");

        currentInputCarga = new TextField();
        currentInputCarga.setPromptText(item.cargaPrevista() != null ? formatarKg(item.cargaPrevista()) : "Carga");
        currentInputCarga.getStyleClass().add("input-set");
        currentInputCarga.setPrefWidth(95.0);

        currentInputReps = new TextField();
        currentInputReps.setPromptText(item.repsPrevistas() != null ? String.valueOf(item.repsPrevistas()) : "Reps");
        currentInputReps.getStyleClass().add("input-set");
        currentInputReps.setPrefWidth(60.0);
        currentInputReps.setOnAction(e -> handleConfirmarSerie()); // Enter confirma a série

        grid.add(lblNum, 0, 0);
        grid.add(currentInputCarga, 1, 0);
        grid.add(currentInputReps, 2, 0);
        grid.add(criarIcone("/icons/Ellipse.png"), 3, 0);
        return grid;
    }

    private GridPane criarLinhaPendente(int numero) {
        GridPane grid = criarGradeLinha();
        grid.getStyleClass().add("set-row");
        grid.setPrefHeight(45.0);
        grid.setStyle("-fx-opacity: 0.5;");

        Label lblNum = new Label(String.valueOf(numero));
        lblNum.getStyleClass().add("set-number");

        Label lblCarga = new Label("—");
        lblCarga.setStyle("-fx-text-fill: #888888;");
        Label lblReps = new Label("—");
        lblReps.setStyle("-fx-text-fill: #888888;");

        grid.add(lblNum, 0, 0);
        grid.add(lblCarga, 1, 0);
        grid.add(lblReps, 2, 0);
        grid.add(criarIcone("/icons/Ellipse.png"), 3, 0);
        return grid;
    }

    // ------------------------------------------------------------------
    // Ações da tela
    // ------------------------------------------------------------------

    @FXML
    private void handleConfirmarSerie() {
        if (itens.isEmpty()) return;

        ItemTreinoDTO item = itens.get(itemIndex);
        List<SerieExecutadaDTO> series = seriesPorItem.get(itemIndex);

        if (series.size() >= valor(item.seriesPrevistas())) {
            exibirNotificacaoNaTela("Todas as séries deste exercício já foram concluídas!");
            return;
        }
        if (currentInputCarga == null || currentInputReps == null) return;

        String cargaStr = textoDe(currentInputCarga);
        String repsStr = textoDe(currentInputReps);

        if (cargaStr.isEmpty() || repsStr.isEmpty()) {
            exibirNotificacaoNaTela("Preencha Carga e Repetições antes de confirmar!");
            return;
        }

        try {
            double carga = Double.parseDouble(cargaStr.replace(',', '.'));
            int reps = Integer.parseInt(repsStr);

            if (carga < 0 || reps <= 0) {
                exibirNotificacaoNaTela("Informe uma carga válida e pelo menos 1 repetição.");
                return;
            }

            int numeroSerie = series.size() + 1;
            double volumeCalculado = carga * reps;

            SerieExecutadaDTO novaSerie = new SerieExecutadaDTO(
                    null,
                    numeroSerie,
                    carga,
                    reps,
                    LocalTime.now().format(FORMATO_HORA),
                    volumeCalculado,
                    true,
                    item.id());

            series.add(novaSerie);
            exibirNotificacaoNaTela("Série " + numeroSerie + " confirmada! (" + formatarKg(carga) + "kg x " + reps + " reps)");
            renderizarSeries(); // atualiza a UI instantaneamente (grade, progresso e volumes)

        } catch (NumberFormatException e) {
            exibirNotificacaoNaTela("Valores numéricos inválidos para Carga/Reps.");
        }
    }

    @FXML
    private void handleProximoExercicio() {
        if (itemIndex < itens.size() - 1) {
            itemIndex++;
            renderizarItemAtual();
        }
    }

    @FXML
    private void handleExercicioAnterior() {
        if (itemIndex > 0) {
            itemIndex--;
            renderizarItemAtual();
        }
    }

    @FXML
    private void handleVoltar() {
        boolean temProgresso = seriesPorItem.stream().anyMatch(l -> !l.isEmpty());
        if (temProgresso) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Sair do treino");
            alert.setHeaderText("Sair sem finalizar?");
            alert.setContentText("As séries registradas nesta sessão ainda não foram salvas.");
            if (alert.showAndWait().filter(r -> r == ButtonType.OK).isEmpty()) return;
        }
        pararCronometro();
        NavigationManager.navegarPara(containerSeries, "/fxml/dashboard_home_view.fxml");
    }

    @FXML
    private void handleFinalizarTreino() {
        if (finalizando || treinoAtivo == null) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Finalizar Treino");
        alert.setHeaderText("Deseja concluir o treino agora?");
        alert.setContentText("O seu progresso de hoje será salvo.");

        if (alert.showAndWait().filter(r -> r == ButtonType.OK).isEmpty()) return;

        List<SerieExecutadaDTO> todasAsSeries = seriesPorItem.stream().flatMap(List::stream).toList();
        RegistroHistoricoDTO registro = new RegistroHistoricoDTO(
                treinoAtivo.treinoId(), segundosDecorridos, todasAsSeries);

        finalizando = true;
        pararCronometro();

        FxAsync.executar(
                () -> {
                    treinoApiClient.finalizarTreino(registro);
                    return Boolean.TRUE;
                },
                ok -> NavigationManager.navegarPara(containerSeries, "/fxml/dashboard_home_view.fxml"),
                erro -> {
                    finalizando = false;
                    iniciarCronometro(); // a sessão continua ativa: nada foi salvo
                    FxAsync.exibirErro("Não foi possível finalizar o treino", erro);
                });
    }

    // ------------------------------------------------------------------
    // Utilitários
    // ------------------------------------------------------------------

    private static int valor(Integer v) {
        return v != null ? v : 0;
    }

    private static double volumeDe(SerieExecutadaDTO serie) {
        if (serie.volumeCalculado() != null) return serie.volumeCalculado();
        double carga = serie.cargaUtilizada() != null ? serie.cargaUtilizada() : 0.0;
        return carga * valor(serie.repeticoesRealizadas());
    }

    /** 80.0 -> "80"; 82.5 -> "82.5". */
    private static String formatarKg(Double kg) {
        if (kg == null) return "0";
        return kg == Math.rint(kg) ? String.valueOf(kg.longValue()) : String.valueOf(kg);
    }

    private static String textoDe(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private ImageView criarIcone(String caminho) {
        ImageView view = new ImageView();
        var stream = getClass().getResourceAsStream(caminho);
        if (stream != null) {
            view.setImage(new Image(stream));
            view.setFitWidth(20.0);
            view.setFitHeight(20.0);
        }
        return view;
    }

    private void exibirNotificacaoNaTela(String mensagem) {
        if (lblNotificacaoTexto == null || boxNotificacao == null) return;

        lblNotificacaoTexto.setText(mensagem);
        boxNotificacao.setManaged(true);
        boxNotificacao.setVisible(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            boxNotificacao.setVisible(false);
            boxNotificacao.setManaged(false);
        });
        delay.play();
    }
}
