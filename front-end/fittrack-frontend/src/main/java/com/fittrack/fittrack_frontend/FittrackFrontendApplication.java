package com.fittrack.fittrack_frontend;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import javafx.application.Application;

@SpringBootApplication
public class FittrackFrontendApplication {

	public static void main(String[] args) {
		Application.launch(JavaFxApplication.class, args);
		System.err.println("Programa inicializado com sucesso.");
	}

}
