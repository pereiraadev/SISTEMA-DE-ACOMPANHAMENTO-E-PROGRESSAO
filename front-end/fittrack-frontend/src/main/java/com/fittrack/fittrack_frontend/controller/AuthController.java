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

/**
 * Controla as telas de entrada e cadastro definidas em {@code auth_view.fxml}.
 * Alterna os modos do formulário, valida os campos e delega autenticação à API sem bloquear a UI.
 */
@Controller
@Scope("prototype")
public class AuthController {

    /** Aba que ativa o formulário de login. */
    @FXML private Button btnTabEntrar;
    /** Aba que ativa o formulário de criação de conta. */
    @FXML private Button btnTabCadastrar;
    /** Contêiner do nome, visível apenas durante o cadastro. */
    @FXML private VBox boxNome;
    /** Campo do nome completo para cadastro. */
    @FXML private TextField txtNome;
    /** Campo de e-mail usado tanto no login quanto no cadastro. */
    @FXML private TextField txtEmail;
    /** Campo de senha mascarado. */
    @FXML private PasswordField txtSenhaOculta;
    /** Campo alternativo de senha em texto para a opção de visualização. */
    @FXML private TextField txtSenhaVisivel;
    /** Botão que submete o formulário conforme o modo ativo. */
    @FXML private Button btnAcaoPrincipal;
    /** Texto contextual do rodapé que oferece a troca de modo. */
    @FXML private Text lblRodapeTexto;
    /** Rótulo clicável do rodapé para alternar login/cadastro. */
    @FXML private Text lblRodapeLink;

    /** Define qual conjunto de campos e qual endpoint serão usados no envio. */
    private boolean modoCadastro = true;
    /** Controla qual dos campos de senha é apresentado. */
    private boolean senhaVisivel = false;

    /** Cliente responsável pelos endpoints de login e cadastro. */
    private final PraticanteApiClient praticanteApiClient;

    /**
     * Cria o controller com o cliente de autenticação fornecido pelo Spring.
     *
     * @param praticanteApiClient cliente REST de operações do praticante
     */
    public AuthController(PraticanteApiClient praticanteApiClient) {
        this.praticanteApiClient = praticanteApiClient;
    }

    /**
     * Configura bindings entre campos de senha e atualiza os rótulos iniciais após o carregamento FXML.
     */
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
        // A troca de visibilidade conserva o valor porque os campos compartilham um binding bidirecional.
        senhaVisivel = !senhaVisivel;
        if (txtSenhaOculta != null) txtSenhaOculta.setVisible(!senhaVisivel);
        if (txtSenhaVisivel != null) txtSenhaVisivel.setVisible(senhaVisivel);
    }

    @FXML
    private void handleSwitchToEntrar() {
        // Evento de clique na aba Entrar: troca o estado e redesenha os elementos vinculados ao modo.
        modoCadastro = false;
        atualizarInterface();
    }

    @FXML
    private void handleSwitchToCadastrar() {
        // Evento de clique na aba Cadastrar: mostra o campo adicional de nome.
        modoCadastro = true;
        atualizarInterface();
    }

    @FXML
    private void handleAlternarModo() {
        // Link de rodapé alterna entre os dois formulários sem descartar os valores digitados.
        modoCadastro = !modoCadastro;
        atualizarInterface();
    }


    @FXML
    private void handleAcaoPrincipal() {
        // Evento de submissão do formulário: valida entradas localmente antes de construir o DTO.
        String email = (txtEmail != null && txtEmail.getText() != null) ? txtEmail.getText().trim() : "";
        String senha = getSenhaDigitada();

        if (email.isEmpty() || senha.isEmpty()) {
            exibirAlerta("Campos Obrigatórios", "Por favor, preencha e-mail e senha para prosseguir.");
            return;
        }

        // O modo determina o payload e endpoint; ambos retornam o mesmo formato de sessão.
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

        // Evita submissões repetidas enquanto a chamada de rede está em andamento.
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

    /** Devolve o conteúdo do campo que está visível, mantendo o formulário independente do modo visual. */
    private String getSenhaDigitada() {
        if (senhaVisivel && txtSenhaVisivel != null && txtSenhaVisivel.getText() != null) {
            return txtSenhaVisivel.getText().trim();
        } else if (txtSenhaOculta != null && txtSenhaOculta.getText() != null) {
            return txtSenhaOculta.getText().trim();
        }
        return "";
    }

    /**
     * Sincroniza visibilidade, textos e classes CSS com o modo de autenticação atual.
     * {@code managed} também retira o campo de nome do cálculo de layout quando ele não é aplicável.
     */
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

    /** Exibe validações locais em uma caixa modal antes de iniciar uma requisição REST. */
    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void handleLimparCampos() {
        // Evento de cancelamento/limpeza: remove dados transitórios do formulário visível e oculto.
        if (txtNome != null) txtNome.clear();
        if (txtEmail != null) txtEmail.clear();
        if (txtSenhaOculta != null) txtSenhaOculta.clear();
        if (txtSenhaVisivel != null) txtSenhaVisivel.clear();
    }
}
