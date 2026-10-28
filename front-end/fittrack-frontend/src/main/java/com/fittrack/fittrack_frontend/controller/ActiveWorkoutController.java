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
 * Controla {@code active_workout_view.fxml}, capturando séries, progresso e duração da sessão ativa.
 * A interface deriva do {@link TreinoAtivoDTO}: o item selecionado define o exercício e as linhas da grade.
 */
@Controller
@Scope("prototype") // uma instância nova a cada carregamento da tela (estado da sessão não vaza)
public class ActiveWorkoutController {

    /** Formato usado para registrar o horário local em que cada série foi confirmada. */
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    /** Nome da ficha ou sessão no cabeçalho da tela. */
    @FXML private Label txtNomeTreino;
    /** Exercício atualmente selecionado para execução. */
    @FXML private Label txtNomeExercicio;
    /** Grupo muscular do exercício atual. */
    @FXML private Label txtGrupoMuscular;
    /** Prescrição prevista que serve de referência ao praticante. */
    @FXML private Label txtReferenciaAnterior;
    /** Duração atualizada pelo Timeline JavaFX a cada segundo. */
    @FXML private Label txtCronometro;
    /** Quantidade de séries concluídas em relação às previstas. */
    @FXML private Label txtProgressoSeries;
    /** Posição do exercício atual na sessão. */
    @FXML private Label txtProgressoExercicios;
    /** Contêiner em que as linhas das séries são criadas dinamicamente. */
    @FXML private VBox containerSeries;
    /** Volume calculado para a série mais recente. */
    @FXML private Label txtVolumeSerie;
    /** Volume acumulado para o exercício atual. */
    @FXML private Label txtVolumeExercicio;
    /** Botão que seleciona o exercício anterior. */
    @FXML private Button btnExercicioAnterior;
    /** Botão que seleciona o próximo exercício. */
    @FXML private Button btnProximoExercicio;

    /** Contêiner temporário da notificação de confirmação/validação. */
    @FXML private HBox boxNotificacao;
    /** Mensagem exibida pela notificação temporária. */
    @FXML private Label lblNotificacaoTexto;

    /** Cliente REST usado para persistir e finalizar a sessão. */
    private final TreinoApiClient treinoApiClient;

    /** Sessão carregada da API, necessária para montar o payload de finalização. */
    private TreinoAtivoDTO treinoAtivo;
    /** Itens ordenados conforme a prescrição retornada pela API. */
    private List<ItemTreinoDTO> itens = List.of();
    /** Séries concluídas de cada item (mesma posição de {@link #itens}). */
    private final List<List<SerieExecutadaDTO>> seriesPorItem = new ArrayList<>();
    /** Índice do exercício renderizado na lista de itens. */
    private int itemIndex = 0;

    /** Segundos decorridos, inicializados com o valor persistido ao retomar a sessão. */
    private long segundosDecorridos = 0;
    /** Timeline JavaFX que atualiza o cronômetro sem criar uma thread de UI manualmente. */
    private Timeline timeline;
    /** Impede solicitações duplicadas enquanto a finalização está em andamento. */
    private boolean finalizando = false;

    /** Campo editável da carga da série ativa, recriado a cada renderização. */
    private TextField currentInputCarga;
    /** Campo editável de repetições da série ativa, recriado a cada renderização. */
    private TextField currentInputReps;

    /**
     * Cria o controller com o cliente que persiste a conclusão do treino.
     *
     * @param treinoApiClient cliente REST de sessões
     */
    public ActiveWorkoutController(TreinoApiClient treinoApiClient) {
        this.treinoApiClient = treinoApiClient;
    }

    /** Limpa a área dinâmica antes do DTO de treino ser entregue pelo fluxo de navegação. */
    @FXML
    public void initialize() {
        if (containerSeries != null) containerSeries.getChildren().clear();
    }

    // ------------------------------------------------------------------
    // Carregamento do treino
    // ------------------------------------------------------------------

    /**
     * Recebe a sessão da API, recupera séries já concluídas e monta a tela do exercício pendente.
     *
     * @param treino sessão ativa retornada ao iniciar ou retomar o treino
     */
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

    /** Inicia ou reinicia o Timeline para que a duração continue atualizada na thread JavaFX. */
    private void iniciarCronometro() {
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            segundosDecorridos++;
            atualizarCronometro();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /** Interrompe o Timeline ao sair da tela ou durante a persistência da finalização. */
    private void pararCronometro() {
        if (timeline != null) timeline.stop();
    }

    /** Converte segundos decorridos em horas/minutos/segundos e atualiza o Label injetado. */
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

    /**
     * Sincroniza textos e estado dos botões com o item selecionado e reconstrói a grade de séries.
     */
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

    /**
     * Remove e recria as linhas conforme concluídas, ativa e pendentes, recalculando os volumes.
     * A substituição dos filhos do VBox faz o JavaFX atualizar o layout sem manter nós obsoletos.
     */
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

    /** Atualiza contagem e volumes apresentados junto à grade após cada renderização. */
    private void atualizarResumo(int concluidas, int previstas, double volumeSerie, double volumeExercicio) {
        if (txtProgressoSeries != null) txtProgressoSeries.setText(concluidas + " de " + previstas + " séries");
        if (txtVolumeSerie != null) txtVolumeSerie.setText(String.format("%.0f kg", volumeSerie));
        if (txtVolumeExercicio != null) txtVolumeExercicio.setText(String.format("%.0f kg", volumeExercicio));
    }

    /** Cria uma grade reutilizável e define a proporção de largura de cada coluna da série. */
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

    /** Monta a linha não editável de uma série salva, incluindo o indicador visual de conclusão. */
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

    /**
     * Monta a linha editável atual e conecta Enter no campo de repetições ao evento de confirmação.
     *
     * @param numero número sequencial da série ativa
     * @param item prescrição usada como placeholder dos campos
     * @return nó JavaFX com os campos de entrada da série
     */
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

    /** Cria uma linha visualmente atenuada para uma série ainda não iniciada. */
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
        // Evento de botão/Enter: valida campos numéricos e limites antes de adicionar a série ao estado.
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
        // A navegação altera somente o índice local; renderizarItemAtual atualiza os Nodes da tela.
        if (itemIndex < itens.size() - 1) {
            itemIndex++;
            renderizarItemAtual();
        }
    }

    @FXML
    private void handleExercicioAnterior() {
        // Retorna ao item anterior sem modificar as séries já registradas na sessão local.
        if (itemIndex > 0) {
            itemIndex--;
            renderizarItemAtual();
        }
    }

    @FXML
    private void handleVoltar() {
        // Se há progresso ainda não enviado, pede confirmação antes de abandonar a tela.
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
        // O clique pede confirmação, monta um único payload e persiste fora da thread JavaFX.
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

    /** Trata valores de prescrição ausentes como zero ao compor a apresentação. */
    private static int valor(Integer v) {
        return v != null ? v : 0;
    }

    /** Usa volume recebido quando disponível; caso contrário recompõe carga vezes repetições. */
    private static double volumeDe(SerieExecutadaDTO serie) {
        if (serie.volumeCalculado() != null) return serie.volumeCalculado();
        double carga = serie.cargaUtilizada() != null ? serie.cargaUtilizada() : 0.0;
        return carga * valor(serie.repeticoesRealizadas());
    }

    /** Remove casas decimais desnecessárias para exibir pesos como "80" ou "82.5". */
    private static String formatarKg(Double kg) {
        if (kg == null) return "0";
        return kg == Math.rint(kg) ? String.valueOf(kg.longValue()) : String.valueOf(kg);
    }

    /** Normaliza o texto do campo antes da validação e conversão numérica. */
    private static String textoDe(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    /** Cria um ícone de status a partir dos recursos visuais empacotados na aplicação. */
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

    /** Exibe uma notificação não modal e agenda sua remoção do layout após alguns segundos. */
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
