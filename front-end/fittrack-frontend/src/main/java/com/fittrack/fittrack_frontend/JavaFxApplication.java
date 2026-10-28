package com.fittrack.fittrack_frontend;

import com.fittrack.fittrack_frontend.util.NavigationManager;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


public class JavaFxApplication extends Application {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        // Inicializa o contexto do Spring Boot
        this.applicationContext = new SpringApplicationBuilder(FittrackFrontendApplication.class).run();
        
        // Passa o contexto do Spring para o NavigationManager utilizar nas trocas de tela
        NavigationManager.setApplicationContext(applicationContext);
    }

    @Override
    public void start(Stage stage) throws Exception {
        var resource = getClass().getResource("/fxml/dashboard_home_view.fxml");
        
        if (resource == null) {
            System.err.println("ERRO: O arquivo FXML não foi encontrado em 'src/main/resources/fxml/auth_view.fxml'.");
            System.err.println("Verifique se o nome do arquivo e a pasta estão corretos.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(resource);
        loader.setControllerFactory(applicationContext::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root, 380, 720);

        stage.setTitle("FitTrack");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        if (this.applicationContext != null) {
            this.applicationContext.close();
        }
        Platform.exit();
    }

}
