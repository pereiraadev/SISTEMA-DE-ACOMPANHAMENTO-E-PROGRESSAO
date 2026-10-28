package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.dto.AuthResponseDTO;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Controller;

@Controller
public class MainController {

    @FXML private BorderPane rootLayout;
    @FXML private StackPane contentArea;

    private static AuthResponseDTO usuarioLogado;

    @FXML
    public void initialize() {
        // Inicializa o container principal e monitora o estado global da aplicação
    }

    /**
     * Registra o usuário autenticado na sessão global do front-end após o login.
     */
    public static void setUsuarioLogado(AuthResponseDTO usuario) {
        usuarioLogado = usuario;
    }

    /**
     * Retorna os dados do usuário autenticado na sessão atual.
     */
    public static AuthResponseDTO getUsuarioLogado() {
        return usuarioLogado;
    }

    /**
     * Id do praticante autenticado (usado nas chamadas à API), ou null se não houver sessão.
     */
    public static Long getUsuarioLogadoId() {
        return usuarioLogado != null ? usuarioLogado.praticanteId() : null;
    }

    /**
     * Limpa a sessão global (Logout) e redireciona o usuário para a tela de autenticação.
     */
    public void efetuarLogout(Node nodeOrigem) {
        usuarioLogado = null;
        if (nodeOrigem != null) {
            NavigationManager.navegarPara(nodeOrigem, "/fxml/auth_view.fxml");
        }
    }

    /**
     * Troca dinamicamente o nó visível na área central da aplicação quando estruturado em Layout Shell.
     */
    public void carregarConteudoCentral(Node noConteudo) {
        if (contentArea != null && noConteudo != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(noConteudo);
        }
    }
}
