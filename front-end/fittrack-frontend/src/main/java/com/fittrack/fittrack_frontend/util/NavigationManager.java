package com.fittrack.fittrack_frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URL;

/**
 * Centraliza a troca de telas do JavaFX carregando FXML em uma nova Scene do Stage atual.
 * Quando o contexto Spring está disponível, a fábrica de controllers permite injeção de dependências
 * também nas telas criadas dinamicamente.
 */
public class NavigationManager {

    /** Contexto usado para obter controllers Spring com seus clientes e serviços injetados. */
    private static ConfigurableApplicationContext applicationContext;

    /**
     * Registra o contexto Spring inicializado pela aplicação JavaFX.
     *
     * @param context contexto responsável pela criação dos controllers
     */
    public static void setApplicationContext(ConfigurableApplicationContext context) {
        applicationContext = context;
    }

    /**
     * Carrega outro FXML e substitui a cena da janela que contém o nó de origem.
     *
     * @param nodeOrigem nó da tela atual usado para localizar o Stage
     * @param fxmlPath caminho absoluto do recurso FXML no classpath
     */
    public static void navegarPara(Node nodeOrigem, String fxmlPath) {
        try {
            Stage stage = (Stage) nodeOrigem.getScene().getWindow();
            URL resource = NavigationManager.class.getResource(fxmlPath);

            if (resource == null) {
                System.err.println("ERRO DE NAVEGAÇÃO: Arquivo FXML não encontrado em 'src/main/resources" + fxmlPath + "'");
                return;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            if (applicationContext != null) {
                loader.setControllerFactory(applicationContext::getBean);
            }

            Parent root = loader.load();
            Scene scene = new Scene(root, 380, 720);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar a tela: " + fxmlPath);
        }
    }

    /**
     * Navega para uma tela e devolve o controller criado pelo FXMLLoader.
     * O chamador pode então passar dados de navegação ao controller, por exemplo, o treino selecionado.
     *
     * @param nodeOrigem nó da tela atual usado para localizar o Stage
     * @param fxmlPath caminho absoluto do recurso FXML no classpath
     * @param <T> tipo esperado para o controller do FXML
     * @return controller carregado ou {@code null} se o recurso não puder ser carregado
     */
    public static <T> T navegarComController(Node nodeOrigem, String fxmlPath) {
        try {
            Stage stage = (Stage) nodeOrigem.getScene().getWindow();
            URL resource = NavigationManager.class.getResource(fxmlPath);

            if (resource == null) {
                System.err.println("ERRO DE NAVEGAÇÃO: Arquivo FXML não encontrado em 'src/main/resources" + fxmlPath + "'");
                return null;
            }

            FXMLLoader loader = new FXMLLoader(resource);
            if (applicationContext != null) {
                loader.setControllerFactory(applicationContext::getBean);
            }

            Parent root = loader.load();
            Scene scene = new Scene(root, 380, 720);
            stage.setScene(scene);
            stage.show();

            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erro ao carregar o controller da tela: " + fxmlPath);
            return null;
        }
    }
}
