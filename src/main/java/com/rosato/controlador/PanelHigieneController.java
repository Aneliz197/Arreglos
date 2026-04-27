package com.rosato.controlador;

import com.rosato.util.Navegador;
import javafx.fxml.FXML;

public class PanelHigieneController {

    @FXML private void onRevisiones() {
        try { Navegador.ir("ListadoRevisionesHigiene.fxml"); } catch (Exception ignored) { }
    }
    @FXML private void onControles() {
        try { Navegador.ir("ListadoControlSanitario.fxml"); } catch (Exception ignored) { }
    }
    @FXML private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
