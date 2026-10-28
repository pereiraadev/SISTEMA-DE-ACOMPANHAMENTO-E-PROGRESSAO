package com.fittrack.fittrack_frontend;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;

/**
 * Ponto de entrada do módulo JavaFX integrado ao contexto Spring Boot.
 * A inicialização da UI é delegada a {@link JavaFxApplication}, que cria a janela e as views.
 */
@SpringBootApplication
public class FittrackFrontendApplication {

	/**
	 * Inicia o ciclo de vida JavaFX; o contexto Spring é criado pela aplicação JavaFX.
	 *
	 * @param args argumentos de inicialização recebidos pelo processo
	 */
	public static void main(String[] args) {
		Application.launch(JavaFxApplication.class, args);
		System.err.println("Programa inicializado com sucesso.");
	}

}
