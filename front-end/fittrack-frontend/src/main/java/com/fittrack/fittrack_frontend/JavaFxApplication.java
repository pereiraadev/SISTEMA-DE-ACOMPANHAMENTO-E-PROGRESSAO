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

/**
 * Coordena o ciclo de vida da janela JavaFX e do contexto Spring Boot.
 * O FXMLLoader usa o contexto para construir controllers com suas dependências injetadas.
 */
public class JavaFxApplication extends Application {

    /** Contexto Spring mantido enquanto a janela principal estiver aberta. */
    private ConfigurableApplicationContext applicationContext;

    /**
     * Inicializa o contexto Spring antes de iniciar a thread da interface JavaFX.
     * O NavigationManager recebe o mesmo contexto para carregar controllers nas próximas cenas.
     */
    @Override
    public void init() {
        // Inicializa o contexto do Spring Boot
        this.applicationContext = new SpringApplicationBuilder(FittrackFrontendApplication.class).run();
        
        // Passa o contexto do Spring para o NavigationManager utilizar nas trocas de tela
        NavigationManager.setApplicationContext(applicationContext);
    }

    /**
     * Carrega o FXML inicial, cria a Scene e configura a janela principal do FitTrack.
     *
     * @param stage janela primária fornecida pelo JavaFX
     * @throws Exception propagada quando o FXML inicial não puder ser carregado
     */
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

    /**
     * Fecha o contexto Spring e encerra o runtime JavaFX ao terminar a aplicação.
     */
    @Override
    public void stop() {
        if (this.applicationContext != null) {
            this.applicationContext.close();
        }
        Platform.exit();
    }

}
