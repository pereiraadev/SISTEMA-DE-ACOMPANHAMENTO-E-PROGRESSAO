package com.fittrack.fittrack_frontend.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.net.URL;

public class NavigationManager {

    private static ConfigurableApplicationContext applicationContext;

    public static void setApplicationContext(ConfigurableApplicationContext context) {
        applicationContext = context;
    }

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
