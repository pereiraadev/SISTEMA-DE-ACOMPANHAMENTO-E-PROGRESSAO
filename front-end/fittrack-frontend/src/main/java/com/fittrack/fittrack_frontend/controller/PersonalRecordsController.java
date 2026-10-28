package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.client.PraticanteApiClient;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.RecordesPessoaisDTO;
import com.fittrack.fittrack_frontend.util.FxAsync;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import java.io.InputStream;
import java.util.List;

/**
 * Controla {@code personal_records_view.fxml}, apresentando recordes agregados e por exercício.
 * Cada cartão pode encaminhar o exercício selecionado à tela de histórico detalhado.
 */
@Controller
@Scope("prototype")
public class PersonalRecordsController {

    /** Maior carga usada como destaque principal. */
    @FXML private Label lblMaiorCargaDestaque;
    /** Nome do exercício associado ao recorde principal. */
    @FXML private Label lblExercicioDestaque;
    /** Rótulo de data do destaque, se fornecido pela view. */
    @FXML private Label lblDataDestaque;

    /** Maior carga absoluta em qualquer exercício. */
    @FXML private Label lblMaiorCargaAbsoluta;
    /** Maior volume acumulado em um treino. */
    @FXML private Label lblMaiorVolumeTreino;
    /** Maior combinação de carga e repetições. */
    @FXML private Label lblMaiorRepsSobCarga;
    /** Maior sequência de semanas com frequência de treino. */
    @FXML private Label lblMaiorFrequenciaSemanas;

    /** Contêiner preenchido com os cartões de recorde por exercício. */
    @FXML private VBox containerPRsExercicio;

    /** Cliente que consulta os recordes do praticante autenticado. */
    private final PraticanteApiClient praticanteApiClient;

    /**
     * Cria a tela com acesso às operações de recordes da API.
     *
     * @param praticanteApiClient cliente REST do praticante
     */
    public PersonalRecordsController(PraticanteApiClient praticanteApiClient) {
        this.praticanteApiClient = praticanteApiClient;
    }

    /** Limpa cards antigos e inicia a busca assíncrona de recordes após carregar a view. */
    @FXML
    public void initialize() {
        limparContainers();

        Long praticanteId = MainController.getUsuarioLogadoId();
        if (praticanteId == null) return;

        FxAsync.executar(
                () -> praticanteApiClient.buscarRecordesPessoais(praticanteId),
                this::carregarRecordesPessoais,
                erro -> FxAsync.exibirErro("Não foi possível carregar os recordes pessoais", erro));
    }

    /** Remove nós gerados anteriormente antes de exibir uma nova resposta da API. */
    private void limparContainers() {
        if (containerPRsExercicio != null) containerPRsExercicio.getChildren().clear();
    }

    /**
     * Injeta e exibe os dados reais do Hall da Fama / Recordes Pessoais trazidos da API Spring Boot.
     *
     * @param recordes recordes agregados e individuais do praticante
     */
    public void carregarRecordesPessoais(RecordesPessoaisDTO recordes) {
        limparContainers();

        if (recordes == null) return;

        // 1. Preenchimento dos Cards de Destaque e Métricas Gerais
        if (lblMaiorCargaDestaque != null && recordes.maiorCargaAbsoluta() != null) {
            lblMaiorCargaDestaque.setText(String.format("%.0f kg", recordes.maiorCargaAbsoluta()));
        }

        if (lblExercicioDestaque != null && recordes.exercicioMaiorCarga() != null) {
            lblExercicioDestaque.setText(recordes.exercicioMaiorCarga().toUpperCase());
        }

        if (lblMaiorCargaAbsoluta != null && recordes.maiorCargaAbsoluta() != null) {
            lblMaiorCargaAbsoluta.setText(String.format("%.0f kg", recordes.maiorCargaAbsoluta()));
        }

        if (lblMaiorVolumeTreino != null && recordes.maiorVolumeTreino() != null) {
            lblMaiorVolumeTreino.setText(String.format("%,.0f kg", recordes.maiorVolumeTreino()));
        }

        if (lblMaiorRepsSobCarga != null && recordes.maiorRepsSobCarga() != null) {
            lblMaiorRepsSobCarga.setText(recordes.maiorRepsSobCarga());
        }

        if (lblMaiorFrequenciaSemanas != null && recordes.maiorFrequenciaSemanas() != null) {
            lblMaiorFrequenciaSemanas.setText(recordes.maiorFrequenciaSemanas() + " semanas");
        }

        // 2. Renderização dos PRs por Exercício
        if (recordes.prsPorExercicio() != null && !recordes.prsPorExercicio().isEmpty()) {
            renderizarListaPRs(recordes.prsPorExercicio());
        } else {
            exibirMensagemVazia();
        }
    }

    /** Converte cada recorde por exercício em um cartão interativo JavaFX. */
    private void renderizarListaPRs(List<ExercicioDTO> exercicios) {
        for (ExercicioDTO ex : exercicios) {
            String imagePath = obterCaminhoImagemExercicio(ex.nome());
            String titulo = ex.nome();
            String dataText = "Recorde Histórico";
            String valorPR = ex.recordeCarga() != null ? String.format("%.0f kg", ex.recordeCarga()) : "—";
            String evolucao = ex.totalVezesRealizado() != null ? (ex.totalVezesRealizado() + " treinos") : "Ativo";

            adicionarItemPR(imagePath, titulo, dataText, valorPR, evolucao, ex);
        }
    }

    /** Monta e estiliza o cartão com imagem, título, valor do recorde e atalho para detalhes. */
    private void adicionarItemPR(String imagePath, String titulo, String data, String valorPR, String evolucao, ExercicioDTO exercicio) {
        HBox card = new HBox(12.0);
        card.getStyleClass().add("exercise-pr-card");
        card.setAlignment(Pos.CENTER_LEFT);

        if (exercicio != null) {
            card.setOnMouseClicked(e -> abrirDetalheExercicio(exercicio));
        }

        // Instanciação e configuração do ImageView com tratamento de recurso
        ImageView imgView = new ImageView();
        imgView.setFitWidth(48.0);
        imgView.setFitHeight(48.0);
        imgView.setPreserveRatio(false);

        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream != null) {
            imgView.setImage(new Image(imageStream));

            // Clipe para cantos arredondados na imagem
            Rectangle clip = new Rectangle(48.0, 48.0);
            clip.setArcWidth(12.0);
            clip.setArcHeight(12.0);
            imgView.setClip(clip);
        } else {
            // Tenta caminho padrão caso a imagem específica não seja encontrada
            InputStream fallbackStream = getClass().getResourceAsStream("/images/Rectangle (4).png");
            if (fallbackStream != null) {
                imgView.setImage(new Image(fallbackStream));
                Rectangle clip = new Rectangle(48.0, 48.0);
                clip.setArcWidth(12.0);
                clip.setArcHeight(12.0);
                imgView.setClip(clip);
            }
        }

        // Informações do Exercício
        VBox info = new VBox(3.0);
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("exercise-title");

        Label lblData = new Label(data);
        lblData.getStyleClass().add("exercise-date");

        info.getChildren().addAll(lblTitulo, lblData);
        HBox.setHgrow(info, Priority.ALWAYS);

        // Bloco de Valor e Badge de Evolução
        VBox valorBox = new VBox(4.0);
        valorBox.setAlignment(Pos.CENTER_RIGHT);

        HBox pill = new HBox();
        pill.getStyleClass().add("pill-pr-value");
        pill.setAlignment(Pos.CENTER);
        Label lblValor = new Label(valorPR);
        lblValor.getStyleClass().add("pill-pr-text");
        pill.getChildren().add(lblValor);

        Label lblEvolucao = new Label(evolucao);
        lblEvolucao.getStyleClass().add("badge-evolution");

        valorBox.getChildren().addAll(pill, lblEvolucao);

        // Adiciona todos os nós ao card principal
        card.getChildren().addAll(imgView, info, valorBox);

        // Adiciona ao container presente no FXML
        if (containerPRsExercicio != null) {
            containerPRsExercicio.getChildren().add(card);
        }
    }

    /** Escolhe um recurso ilustrativo para o nome do exercício, com imagem padrão para os demais. */
    private String obterCaminhoImagemExercicio(String nomeExercicio) {
        if (nomeExercicio == null) return "/images/Rectangle (4).png";
        
        String lower = nomeExercicio.toLowerCase();
        if (lower.contains("agachamento")) {
            return "/images/Rectangle (1).png";
        } else if (lower.contains("terra")) {
            return "/images/Rectangle (5).png";
        } else {
            return "/images/Rectangle (4).png";
        }
    }

    /** Navega para o histórico e fornece ao controller de destino o exercício selecionado. */
    private void abrirDetalheExercicio(ExercicioDTO exercicio) {
        if (containerPRsExercicio != null) {
            ExerciseDetailsController controller = NavigationManager.navegarComController(containerPRsExercicio, "/fxml/exercise_details_view.fxml");
            if (controller != null && exercicio != null) {
                controller.carregarDetalhesExercicio(exercicio);
            }
        }
    }

    /** Exibe estado vazio quando a API não retorna recordes individuais. */
    private void exibirMensagemVazia() {
        Label lblVazio = new Label("Nenhum recorde pessoal registrado ainda.");
        lblVazio.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        
        if (containerPRsExercicio != null) containerPRsExercicio.getChildren().add(lblVazio);
    }

    @FXML
    private void handleVoltar() {
        // O evento retorna à área de métricas sem descartar a sessão autenticada.
        if (containerPRsExercicio != null) {
            NavigationManager.navegarPara(containerPRsExercicio, "/fxml/metrics_hub_view.fxml");
        }
    }
}
