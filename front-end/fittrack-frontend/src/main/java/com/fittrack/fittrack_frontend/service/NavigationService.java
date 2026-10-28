package com.fittrack.fittrack_frontend.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.stereotype.Service;

@Service
public class NavigationService {
    private final org.springframework.context.ConfigurableApplicationContext context;
    private Stage stage;

    public NavigationService(org.springframework.context.ConfigurableApplicationContext context) {
        this.context = context;
    }

    public void configurar(Stage stage) {
        this.stage = stage;
    }

    public void navegar(String recurso) {
        if (stage == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(recurso));
            loader.setControllerFactory(context::getBean);
            Parent root = loader.load();
            stage.setScene(new Scene(root));
        } catch (Exception erro) {
            throw new IllegalStateException("Não foi possível abrir a tela " + recurso, erro);
        }
    }
}
