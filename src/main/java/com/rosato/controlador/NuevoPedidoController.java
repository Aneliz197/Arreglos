package com.rosato.controlador;

import com.rosato.util.Navegador;
import com.rosato.util.Sesion;
import javafx.fxml.FXML;

/** Pantalla 2.2 - Stub. Se implementará en el siguiente hito. */
public class NuevoPedidoController {

    @FXML
    private void onVolver() {
        try {
            String destino = Sesion.esEmpleado() ? "DashboardEmpleado.fxml" : "DashboardCliente.fxml";
            Navegador.ir(destino);
        } catch (Exception ignored) { }
    }
}
