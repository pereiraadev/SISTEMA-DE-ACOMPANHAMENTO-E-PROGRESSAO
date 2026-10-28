package com.fittrack.fittrack_frontend.controller;

import com.fittrack.fittrack_frontend.dto.AuthResponseDTO;
import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.springframework.stereotype.Controller;

/**
 * Controller do layout principal e ponto de acesso à sessão do usuário no front-end.
 * A autenticação fica em memória durante a execução e é compartilhada com os controllers e o cliente REST.
 */
@Controller
public class MainController {

    /** Layout raiz da tela principal, disponível para a composição do shell visual. */
    @FXML private BorderPane rootLayout;
    /** Área central cujo conteúdo pode ser substituído sem recriar a janela. */
    @FXML private StackPane contentArea;

    /** Usuário autenticado na execução atual, ou {@code null} após logout. */
    private static AuthResponseDTO usuarioLogado;

    /** Ponto de inicialização do FXML principal; os containers são populados conforme a navegação. */
    @FXML
    public void initialize() {
        // Inicializa o container principal e monitora o estado global da aplicação
    }

    /**
     * Registra o usuário autenticado na sessão global do front-end após o login.
     *
     * @param usuario dados de autenticação retornados pela API
     */
    public static void setUsuarioLogado(AuthResponseDTO usuario) {
        usuarioLogado = usuario;
    }

    /**
     * Retorna os dados do usuário autenticado na sessão atual.
     *
     * @return sessão atual ou {@code null} quando não autenticado
     */
    public static AuthResponseDTO getUsuarioLogado() {
        return usuarioLogado;
    }

    /**
     * Id do praticante autenticado (usado nas chamadas à API), ou null se não houver sessão.
     *
     * @return identificador do praticante ou {@code null}
     */
    public static Long getUsuarioLogadoId() {
        return usuarioLogado != null ? usuarioLogado.praticanteId() : null;
    }

    /**
     * Limpa a sessão global (Logout) e redireciona o usuário para a tela de autenticação.
     *
     * @param nodeOrigem nó da cena atual usado para localizar a janela
     */
    public void efetuarLogout(Node nodeOrigem) {
        usuarioLogado = null;
        if (nodeOrigem != null) {
            NavigationManager.navegarPara(nodeOrigem, "/fxml/auth_view.fxml");
        }
    }

    /**
     * Troca dinamicamente o nó visível na área central da aplicação quando estruturado em Layout Shell.
     *
     * @param noConteudo conteúdo JavaFX que será incluído na área central
     */
    public void carregarConteudoCentral(Node noConteudo) {
        if (contentArea != null && noConteudo != null) {
            contentArea.getChildren().clear();
            contentArea.getChildren().add(noConteudo);
        }
    }
}
