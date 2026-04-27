package com.rosato.controlador;

import com.rosato.dao.ControlSanitarioDAO;
import com.rosato.dao.EmpleadoDAO;
import com.rosato.modelo.ControlSanitarioEmpleado;
import com.rosato.modelo.Empleado;
import com.rosato.util.Navegador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.time.LocalDate;

public class NuevoControlSanitarioController {

    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<Empleado> cbEmpleado;
    @FXML private CheckBox chkUni;
    @FXML private CheckBox chkManos;
    @FXML private CheckBox chkRed;
    @FXML private CheckBox chkGuantes;
    @FXML private CheckBox chkUnas;
    @FXML private CheckBox chkSalud;
    @FXML private TextArea txtObs;
    @FXML private Label lblMensaje;

    private final ControlSanitarioDAO dao = new ControlSanitarioDAO();
    private final EmpleadoDAO empDao = new EmpleadoDAO();

    @FXML
    private void initialize() {
        dpFecha.setValue(LocalDate.now());
        try {
            cbEmpleado.setItems(FXCollections.observableArrayList(empDao.listarActivos()));
            cbEmpleado.setConverter(new StringConverter<>() {
                @Override public String toString(Empleado e) {
                    return e == null ? "" : e.getNombreCompleto()
                            + (e.getAreaTrabajo() == null ? "" : " · " + e.getAreaTrabajo());
                }
                @Override public Empleado fromString(String s) { return null; }
            });
        } catch (Exception ex) {
            mostrarError("No se pudieron cargar empleados: " + ex.getMessage());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        if (dpFecha.getValue() == null || cbEmpleado.getValue() == null) {
            mostrarError("Fecha y empleado son obligatorios."); return;
        }
        ControlSanitarioEmpleado c = new ControlSanitarioEmpleado();
        c.setFecha(dpFecha.getValue());
        c.setFkIdEmpleado(cbEmpleado.getValue().getIdEmpleado());
        c.setUniformeOk(chkUni.isSelected());
        c.setManosOk(chkManos.isSelected());
        c.setRedecillaOk(chkRed.isSelected());
        c.setGuantesOk(chkGuantes.isSelected());
        c.setUnasOk(chkUnas.isSelected());
        c.setSaludOk(chkSalud.isSelected());
        String obs = txtObs.getText() == null ? "" : txtObs.getText().trim();
        c.setObservaciones(obs.isEmpty() ? null : obs);
        try {
            dao.insertar(c);
            Navegador.ir("ListadoControlSanitario.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("ListadoControlSanitario.fxml"); } catch (Exception ignored) { }
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
