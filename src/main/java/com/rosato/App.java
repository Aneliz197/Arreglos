package com.rosato;

import com.rosato.util.Navegador;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX.
 * Muestra la pantalla de Login (Módulo 1).
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Navegador.setStage(stage);
        stage.setTitle("Repostería Rosato");
        Navegador.ir("Login.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
