package com.rosato.controlador;

import com.rosato.dao.ProveedorDAO;
import com.rosato.modelo.Proveedor;
import com.rosato.util.Navegador;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EditarProveedorController {

    private static Proveedor seleccionado;
    public static void setSeleccionado(Proveedor p) { seleccionado = p; }

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtRnc;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDireccion;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblMensaje;

    private final ProveedorDAO dao = new ProveedorDAO();

    @FXML
    private void initialize() {
        if (seleccionado != null) {
            lblTitulo.setText("Editar proveedor #" + seleccionado.getIdProveedor());
            txtNombre.setText(seleccionado.getNombre());
            txtRnc.setText(seleccionado.getRnc());
            txtTelefono.setText(seleccionado.getTelefono());
            txtEmail.setText(seleccionado.getEmail());
            txtDireccion.setText(seleccionado.getDireccion());
            chkActivo.setSelected(seleccionado.isActivo());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mostrarError("Nombre obligatorio."); return;
        }
        if (txtTelefono.getText() == null
                || txtTelefono.getText().replaceAll("[^0-9]", "").length() < 10) {
            mostrarError("Teléfono con al menos 10 dígitos."); return;
        }
        String email = txtEmail.getText();
        if (email != null && !email.isBlank() && !email.contains("@")) {
            mostrarError("Email inválido."); return;
        }
        try {
            Proveedor p = seleccionado == null ? new Proveedor() : seleccionado;
            p.setNombre(txtNombre.getText().trim());
            p.setRnc(blankToNull(txtRnc.getText()));
            p.setTelefono(txtTelefono.getText().trim());
            p.setEmail(blankToNull(email));
            p.setDireccion(blankToNull(txtDireccion.getText()));
            p.setActivo(chkActivo.isSelected());
            if (p.getIdProveedor() == null) dao.insertar(p);
            else dao.actualizar(p);
            seleccionado = null;
            Navegador.ir("ListadoProveedores.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionado = null;
        try { Navegador.ir("ListadoProveedores.fxml"); } catch (Exception ignored) { }
    }

    private String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    private void mostrarError(String s) {
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
    private void limpiarMensaje() {
        lblMensaje.setVisible(false); lblMensaje.setManaged(false); lblMensaje.setText("");
    }
}
