package com.fittrack.fittrack_frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.springframework.stereotype.Controller;
import com.fittrack.fittrack_frontend.service.ApiClient;
import com.fittrack.fittrack_frontend.service.AuthModels;

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
    private final ApiClient apiClient;
    private final com.fittrack.fittrack_frontend.service.NavigationService navigationService;

    public AuthController(ApiClient apiClient, com.fittrack.fittrack_frontend.service.NavigationService navigationService) {
        this.apiClient = apiClient;
        this.navigationService = navigationService;
    }

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
        try {
            String email = txtEmail.getText().trim();
            String senha = txtSenhaOculta.getText();
            if (email.isBlank() || senha.isBlank() || (modoCadastro && txtNome.getText().trim().isBlank())) {
                btnAcaoPrincipal.setText("Preencha os campos");
                return;
            }
            if (modoCadastro) {
                apiClient.post("/auth/cadastrar", new AuthModels.Cadastro(txtNome.getText().trim(), email, senha), AuthModels.PraticanteResposta.class);
                modoCadastro = false;
                atualizarInterface();
                btnAcaoPrincipal.setText("Conta criada; entre");
            } else {
                AuthModels.LoginResposta resposta = apiClient.post("/auth/login", new AuthModels.Login(email, senha), AuthModels.LoginResposta.class);
                apiClient.autenticar(resposta.token());
                btnAcaoPrincipal.setText("Login realizado");
                navigationService.navegar("/fxml/dashboard_home_view.fxml");
            }
        } catch (Exception erro) {
            btnAcaoPrincipal.setText("Falha na operação");
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
