package com.rosato.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Utilidad sencilla para cambiar de vista FXML manteniendo un único Stage.
 */
public final class Navegador {

    private static Stage stage;

    private Navegador() { }

    public static void setStage(Stage s) {
        stage = s;
    }

    public static Stage getStage() {
        return stage;
    }

    /** Carga una vista FXML ubicada en /com/rosato/vista/. */
    public static <T> T ir(String nombreFxml) throws IOException {
        Objects.requireNonNull(stage, "Stage no inicializado. Llama a Navegador.setStage() primero.");
        URL url = Navegador.class.getResource("/com/rosato/vista/" + nombreFxml);
        if (url == null) {
            throw new IOException("No se encontró el recurso FXML: " + nombreFxml);
        }
        FXMLLoader loader = new FXMLLoader(url);
        Parent root = loader.load();
        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root);
            URL css = Navegador.class.getResource("/com/rosato/estilos/styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }
        return loader.getController();
    }
}
