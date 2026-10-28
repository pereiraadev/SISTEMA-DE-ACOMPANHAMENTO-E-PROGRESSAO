package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.client.PraticanteApiClient;
import com.fittrack.fittrack_frontend.dto.AuthResponseDTO;
import com.fittrack.fittrack_frontend.dto.CadastroDTO;
import com.fittrack.fittrack_frontend.dto.LoginDTO;
import com.fittrack.fittrack_frontend.util.FxAsync;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.function.Supplier;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

@Controller
@Scope("prototype")
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

    private final PraticanteApiClient praticanteApiClient;

    public AuthController(PraticanteApiClient praticanteApiClient) {
        this.praticanteApiClient = praticanteApiClient;
    }

    @FXML
    public void initialize() {
        if (txtSenhaOculta != null && txtSenhaVisivel != null) {
            // Sincroniza o texto entre o campo de senha oculta e visível
            txtSenhaOculta.textProperty().bindBidirectional(txtSenhaVisivel.textProperty());
        }
        atualizarInterface();
    }

    @FXML
    private void toggleMostrarSenha() {
        senhaVisivel = !senhaVisivel;
        if (txtSenhaOculta != null) txtSenhaOculta.setVisible(!senhaVisivel);
        if (txtSenhaVisivel != null) txtSenhaVisivel.setVisible(senhaVisivel);
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
        String email = (txtEmail != null && txtEmail.getText() != null) ? txtEmail.getText().trim() : "";
        String senha = getSenhaDigitada();

        if (email.isEmpty() || senha.isEmpty()) {
            exibirAlerta("Campos Obrigatórios", "Por favor, preencha e-mail e senha para prosseguir.");
            return;
        }

        Supplier<AuthResponseDTO> chamada;
        if (modoCadastro) {
            String nome = (txtNome != null && txtNome.getText() != null) ? txtNome.getText().trim() : "";
            if (nome.isEmpty()) {
                exibirAlerta("Campo Obrigatório", "Por favor, informe seu nome completo.");
                return;
            }
            CadastroDTO cadastroDTO = new CadastroDTO(nome, email, senha);
            chamada = () -> praticanteApiClient.cadastrar(cadastroDTO);
        } else {
            LoginDTO loginDTO = new LoginDTO(email, senha);
            chamada = () -> praticanteApiClient.login(loginDTO);
        }

        btnAcaoPrincipal.setDisable(true);
        FxAsync.executar(
                chamada,
                resposta -> {
                    btnAcaoPrincipal.setDisable(false);
                    if (resposta == null || resposta.praticanteId() == null) {
                        exibirAlerta("Autenticação", "O servidor não retornou os dados do usuário.");
                        return;
                    }
                    // Sessão global do front-end; o Dashboard busca os próprios dados reais na API.
                    MainController.setUsuarioLogado(resposta);
                    NavigationManager.navegarPara(btnAcaoPrincipal, "/fxml/dashboard_home_view.fxml");
                },
                erro -> {
                    btnAcaoPrincipal.setDisable(false);
                    FxAsync.exibirErro(modoCadastro ? "Falha no cadastro" : "Falha no login", erro);
                });
    }

    private String getSenhaDigitada() {
        if (senhaVisivel && txtSenhaVisivel != null && txtSenhaVisivel.getText() != null) {
            return txtSenhaVisivel.getText().trim();
        } else if (txtSenhaOculta != null && txtSenhaOculta.getText() != null) {
            return txtSenhaOculta.getText().trim();
        }
        return "";
    }

    private void atualizarInterface() {
        if (modoCadastro) {
            if (btnTabCadastrar != null) btnTabCadastrar.getStyleClass().setAll("tab-button-active");
            if (btnTabEntrar != null) btnTabEntrar.getStyleClass().setAll("tab-button-inactive");
            
            if (boxNome != null) {
                boxNome.setManaged(true);
                boxNome.setVisible(true);
            }
            
            if (btnAcaoPrincipal != null) btnAcaoPrincipal.setText("Criar conta");
            if (lblRodapeTexto != null) lblRodapeTexto.setText("Já tem uma conta? ");
            if (lblRodapeLink != null) lblRodapeLink.setText("Entrar");

        } else {
            if (btnTabEntrar != null) btnTabEntrar.getStyleClass().setAll("tab-button-active");
            if (btnTabCadastrar != null) btnTabCadastrar.getStyleClass().setAll("tab-button-inactive");
            
            if (boxNome != null) {
                boxNome.setManaged(false);
                boxNome.setVisible(false);
            }
            
            if (btnAcaoPrincipal != null) btnAcaoPrincipal.setText("Entrar");
            if (lblRodapeTexto != null) lblRodapeTexto.setText("Não tem uma conta? ");
            if (lblRodapeLink != null) lblRodapeLink.setText("Cadastrar-se");
        }
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void handleLimparCampos() {
        if (txtNome != null) txtNome.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtSenhaOculta != null) txtSenhaOculta.clear();
        if (txtSenhaVisivel != null) txtSenhaVisivel.clear();
    }
}
