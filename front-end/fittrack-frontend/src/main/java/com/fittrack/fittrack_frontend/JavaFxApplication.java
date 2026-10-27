package com.fittrack.fittrack_frontend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

public class JavaFxApplication extends Application{
    
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Inicializa o contexto do Spring Boot antes da interface carregar
        this.applicationContext = new SpringApplicationBuilder(FittrackFrontendApplication.class).run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/active_workout_view.fxml"));
        
        // Conecta o FXML ao Injetor de Dependências do Spring
        loader.setControllerFactory(applicationContext::getBean);
        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        stage.setTitle("FitTrack");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        // Encerra o Spring e a aplicação ao fechar a janela
        this.applicationContext.close();
        Platform.exit();
    }
}
