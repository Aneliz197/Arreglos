package com.rosato.controlador;

import com.rosato.util.Navegador;
import com.rosato.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;

public class DashboardEmpleadoController {

    @FXML private Label lblBienvenida;

    @FXML
    private void initialize() {
        String nombre = Sesion.getNombreMostrar() == null ? "" : Sesion.getNombreMostrar();
        String area = Sesion.getAreaTrabajo() == null ? "" : " · " + Sesion.getAreaTrabajo();
        lblBienvenida.setText("Hola, " + nombre + area);
    }

    @FXML private void onClientes()     { ir("ListadoClientes.fxml"); }
    @FXML private void onPedidos()      { ir("ListadoPedidos.fxml"); }
    @FXML private void onNuevoPedido()  { ir("NuevoPedido.fxml"); }
    @FXML private void onInventario()   { ir("ListadoIngredientes.fxml"); }
    @FXML private void onProveedores()  { ir("ListadoProveedores.fxml"); }
    @FXML private void onCompras()      { ir("ListadoCompras.fxml"); }
    @FXML private void onRecetas()      { ir("ListadoRecetas.fxml"); }
    @FXML private void onPlanDia()      { ir("PlanDelDia.fxml"); }
    @FXML private void onEntregas()     { ir("AgendaEntregas.fxml"); }
    @FXML private void onMaquinas()     { ir("ListadoMaquinas.fxml"); }

    @FXML
    private void onCerrarSesion() {
        Sesion.cerrar();
        ir("Login.fxml");
    }

    private void ir(String fxml) {
        try {
            Navegador.ir(fxml);
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo cargar la pantalla '" + fxml + "': " + ex.getMessage()).showAndWait();
        }
    }
}
