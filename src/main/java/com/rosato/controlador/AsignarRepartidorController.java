package com.rosato.controlador;

import com.rosato.dao.EmpleadoDAO;
import com.rosato.dao.EntregaDAO;
import com.rosato.modelo.Empleado;
import com.rosato.modelo.Entrega;
import com.rosato.util.Navegador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

public class AsignarRepartidorController {

    private static Entrega seleccionada;
    public static void setEntrega(Entrega e) { seleccionada = e; }

    @FXML private Label lblCabecera;
    @FXML private ComboBox<Empleado> cbRepartidor;
    @FXML private TextField txtDireccion;
    @FXML private TextArea txtNotas;
    @FXML private Label lblMensaje;

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private final EntregaDAO entregaDAO = new EntregaDAO();

    @FXML
    private void initialize() {
        if (seleccionada == null) { mostrarError("Sin entrega."); return; }
        lblCabecera.setText("Entrega #" + seleccionada.getIdEntrega()
                + " · Pedido #" + seleccionada.getFkIdPedido()
                + " · " + (seleccionada.getNombreCliente() == null ? "" : seleccionada.getNombreCliente()));
        try {
            cbRepartidor.setItems(FXCollections.observableArrayList(empleadoDAO.listarActivos()));
            cbRepartidor.setConverter(new StringConverter<>() {
                @Override public String toString(Empleado e) {
                    return e == null ? "" : e.getNombreCompleto()
                            + (e.getAreaTrabajo() == null ? "" : " · " + e.getAreaTrabajo());
                }
                @Override public Empleado fromString(String s) { return null; }
            });
            if (seleccionada.getFkIdRepartidor() != null) {
                cbRepartidor.getItems().stream()
                        .filter(e -> e.getIdEmpleado().equals(seleccionada.getFkIdRepartidor()))
                        .findFirst().ifPresent(cbRepartidor::setValue);
            }
        } catch (Exception ex) {
            mostrarError("No se pudo cargar empleados: " + ex.getMessage());
        }
        txtDireccion.setText(seleccionada.getDireccion());
        txtNotas.setText(seleccionada.getNotas());
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        if (seleccionada == null) return;
        try {
            Empleado rep = cbRepartidor.getValue();
            String dir = txtDireccion.getText() == null ? null : txtDireccion.getText().trim();
            if (dir != null && dir.isEmpty()) dir = null;
            String nt  = txtNotas.getText() == null ? null : txtNotas.getText().trim();
            if (nt != null && nt.isEmpty()) nt = null;
            entregaDAO.asignarRepartidor(seleccionada.getIdEntrega(),
                    rep == null ? null : rep.getIdEmpleado(), dir, nt);
            seleccionada = null;
            Navegador.ir("AgendaEntregas.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionada = null;
        try { Navegador.ir("AgendaEntregas.fxml"); } catch (Exception ignored) { }
    }

    private void mostrarError(String s) {
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
    private void limpiarMensaje() {
        lblMensaje.setVisible(false); lblMensaje.setManaged(false); lblMensaje.setText("");
    }
}
