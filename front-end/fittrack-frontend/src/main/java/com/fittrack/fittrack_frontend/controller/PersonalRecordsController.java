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

@Controller
@Scope("prototype")
public class PersonalRecordsController {

    @FXML private Label lblMaiorCargaDestaque;
    @FXML private Label lblExercicioDestaque;
    @FXML private Label lblDataDestaque;

    @FXML private Label lblMaiorCargaAbsoluta;
    @FXML private Label lblMaiorVolumeTreino;
    @FXML private Label lblMaiorRepsSobCarga;
    @FXML private Label lblMaiorFrequenciaSemanas;

    @FXML private VBox containerPRsExercicio;

    private final PraticanteApiClient praticanteApiClient;

    public PersonalRecordsController(PraticanteApiClient praticanteApiClient) {
        this.praticanteApiClient = praticanteApiClient;
    }

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

    private void limparContainers() {
        if (containerPRsExercicio != null) containerPRsExercicio.getChildren().clear();
    }

    /**
     * Injeta e exibe os dados reais do Hall da Fama / Recordes Pessoais trazidos da API Spring Boot.
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

    private void abrirDetalheExercicio(ExercicioDTO exercicio) {
        if (containerPRsExercicio != null) {
            ExerciseDetailsController controller = NavigationManager.navegarComController(containerPRsExercicio, "/fxml/exercise_details_view.fxml");
            if (controller != null && exercicio != null) {
                controller.carregarDetalhesExercicio(exercicio);
            }
        }
    }

    private void exibirMensagemVazia() {
        Label lblVazio = new Label("Nenhum recorde pessoal registrado ainda.");
        lblVazio.setStyle("-fx-text-fill: #888888; -fx-font-size: 14px;");
        
        if (containerPRsExercicio != null) containerPRsExercicio.getChildren().add(lblVazio);
    }

    @FXML
    private void handleVoltar() {
        if (containerPRsExercicio != null) {
            NavigationManager.navegarPara(containerPRsExercicio, "/fxml/metrics_hub_view.fxml");
        }
    }
}
