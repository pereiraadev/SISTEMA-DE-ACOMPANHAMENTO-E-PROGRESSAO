package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.springframework.stereotype.Controller;

import java.io.InputStream;

@Controller
public class PersonalRecordsController {

    @FXML private VBox containerPRsExercício;

    @FXML
    public void initialize() {
        carregarPRsSimulados();
    }

    private void carregarPRsSimulados() {
        containerPRsExercício.getChildren().clear();

        // Certifique-se de que estes arquivos existem na pasta src/main/resources/images/
        adicionarItemPR("/images/Rectangle (4).png", "Supino Reto", "Alcançado em 15 Out 2025", "100 kg", "↑ +5 kg");
        adicionarItemPR("/images/Rectangle (1).png", "Agachamento Livre", "Alcançado em 15 Out 2025", "180 kg", "↑ +10 kg");
        adicionarItemPR("/images/Rectangle (5).png", "Levantamento Terra", "Alcançado em 08 Out 2025", "160 kg", "↑ +2 reps");
    }

    private void adicionarItemPR(String imagePath, String titulo, String data, String valorPR, String evolucao) {
        HBox card = new HBox(12.0);
        card.getStyleClass().add("exercise-pr-card");
        card.setAlignment(Pos.CENTER_LEFT);

        // Instanciação e configuração do ImageView com tratamento de recurso
        ImageView imgView = new ImageView();
        imgView.setFitWidth(48.0);
        imgView.setFitHeight(48.0);
        imgView.setPreserveRatio(false);

        InputStream imageStream = getClass().getResourceAsStream(imagePath);
        if (imageStream != null) {
            imgView.setImage(new Image(imageStream));
            
            // Clipe para cantos arredondados na imagem (Border Radius 10px)
            Rectangle clip = new Rectangle(48.0, 48.0);
            clip.setArcWidth(12.0);
            clip.setArcHeight(12.0);
            imgView.setClip(clip);
        } else {
            System.err.println("Aviso: Imagem não encontrada no caminho " + imagePath);
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
        containerPRsExercício.getChildren().add(card);
    }

    @FXML
    private void handleVoltar() {
        System.out.println("Voltar para Métricas do Exercício...");
    }
}
