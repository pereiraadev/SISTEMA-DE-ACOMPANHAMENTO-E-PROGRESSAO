package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.springframework.stereotype.Controller;

@Controller
public class AuthController {

    @FXML private Button btnTabEntrar;
    @FXML private Button btnTabCadastrar;
    @FXML private VBox boxNome;
    @FXML private TextField txtNome;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtSenhaOculta;
    @FXML private TextField txtSenhaVisivel;
    @FXML private Button btnAcaoPrincipal;
    @FXML private Text lblRodapeTexto;
    @FXML private Text lblRodapeLink;

    private boolean modoCadastro = true;
    private boolean senhaVisivel = false;

    @FXML
    public void initialize() {
        // Sincroniza o texto entre o campo de senha oculta e visível
        txtSenhaOculta.textProperty().bindBidirectional(txtSenhaVisivel.textProperty());
        atualizarInterface();
    }

    @FXML
    private void toggleMostrarSenha() {
        senhaVisivel = !senhaVisivel;
        txtSenhaOculta.setVisible(!senhaVisivel);
        txtSenhaVisivel.setVisible(senhaVisivel);
    }

    @FXML
    private void handleSwitchToEntrar() {
        modoCadastro = false;
        atualizarInterface();
    }

    @FXML
    private void handleSwitchToCadastrar() {
        modoCadastro = true;
        atualizarInterface();
    }

    @FXML
    private void handleAlternarModo() {
        modoCadastro = !modoCadastro;
        atualizarInterface();
    }

    @FXML
    private void handleAcaoPrincipal() {
        if (modoCadastro) {
            System.out.println("Cadastrando: " + txtNome.getText() + " | Email: " + txtEmail.getText());
        } else {
            System.out.println("Logando com: " + txtEmail.getText());
        }
    }

    private void atualizarInterface() {
        if (modoCadastro) {
            btnTabCadastrar.getStyleClass().setAll("tab-button-active");
            btnTabEntrar.getStyleClass().setAll("tab-button-inactive");
            boxNome.setManaged(true);
            boxNome.setVisible(true);
            btnAcaoPrincipal.setText("Criar conta");
            lblRodapeTexto.setText("Já tem uma conta? ");
            lblRodapeLink.setText("Entrar");
        } else {
            btnTabEntrar.getStyleClass().setAll("tab-button-active");
            btnTabCadastrar.getStyleClass().setAll("tab-button-inactive");
            boxNome.setManaged(false);
            boxNome.setVisible(false);
            btnAcaoPrincipal.setText("Entrar");
            lblRodapeTexto.setText("Não tem uma conta? ");
            lblRodapeLink.setText("Cadastrar-se");
        }
    }
}
