package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.dto.CadastroDTO;
import com.fittrack.fittrack_frontend.dto.EstatisticasGeraisDTO;
import com.fittrack.fittrack_frontend.dto.ExercicioDTO;
import com.fittrack.fittrack_frontend.dto.LoginDTO;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.List;

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

        String nomeExibicao;

        if (modoCadastro) {
            String nome = (txtNome != null && txtNome.getText() != null) ? txtNome.getText().trim() : "";
            if (nome.isEmpty()) {
                exibirAlerta("Campo Obrigatório", "Por favor, informe seu nome completo.");
                return;
            }
            nomeExibicao = nome;

            // Monta DTO para exibição/validação no console
            CadastroDTO cadastroDTO = new CadastroDTO(nome, email, senha);
            System.out.println("JSON de Cadastro pronto: " + cadastroDTO);

        } else {
            // No modo login, usa a parte do e-mail antes do @ como nome temporário para o teste
            nomeExibicao = email.contains("@") ? email.split("@")[0] : "Atleta";

            // Monta DTO para exibição/validação no console
            LoginDTO loginDTO = new LoginDTO(email, senha);
            System.out.println("JSON de Login pronto: " + loginDTO);
        }

        // Navegação com injeção de dados temporários para teste visual do Front-end
        DashboardHomeController homeController = NavigationManager.navegarComController(btnAcaoPrincipal, "/fxml/dashboard_home_view.fxml");

        if (homeController != null) {
            EstatisticasGeraisDTO statsMock = new EstatisticasGeraisDTO(47, 312, 1248, 45720.0);
            List<ExercicioDTO> exerciciosMock = List.of(
                new ExercicioDTO(1L, "Supino Reto", "Peitoral", "", true, 100.0, 10, null, 12),
                new ExercicioDTO(2L, "Agachamento Livre", "Pernas", "", true, 180.0, 8, null, 15)
            );

            homeController.carregarDadosDashboard(nomeExibicao, statsMock, exerciciosMock);
        }
    }

/*     @FXML
    private void handleAcaoPrincipal() {
        String email = (txtEmail != null && txtEmail.getText() != null) ? txtEmail.getText().trim() : "";
        String senha = getSenhaDigitada();

        if (email.isEmpty() || senha.isEmpty()) {
            exibirAlerta("Campos Obrigatórios", "Por favor, preencha e-mail e senha para prosseguir.");
            return;
        }

        if (modoCadastro) {
            String nome = (txtNome != null && txtNome.getText() != null) ? txtNome.getText().trim() : "";
            if (nome.isEmpty()) {
                exibirAlerta("Campo Obrigatório", "Por favor, informe seu nome completo.");
                return;
            }

            // Instancia o DTO de Cadastro do front-end
            CadastroDTO cadastroDTO = new CadastroDTO(nome, email, senha);
            
            // Prepara o DTO para integração com a API via HttpClient
            if (cadastroDTO.email() != null) {
                System.out.println("DTO de Cadastro montado para: " + cadastroDTO.email());
            }

            // Realiza a navegação para a Home após criar a conta
            NavigationManager.navegarPara(btnAcaoPrincipal, "/fxml/dashboard-home.fxml");

        } else {
            // Instancia o DTO de Login do front-end
            LoginDTO loginDTO = new LoginDTO(email, senha);

            // Prepara o DTO para integração com a API via HttpClient
            if (loginDTO.email() != null) {
                System.out.println("DTO de Login montado para: " + loginDTO.email());
            }

            // Realiza a navegação para a Home após autenticação bem-sucedida
            NavigationManager.navegarPara(btnAcaoPrincipal, "/fxml/dashboard-home.fxml");
        }
    } */

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
