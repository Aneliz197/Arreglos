package com.rosato.controlador;

import com.rosato.dao.EmpleadoDAO;
import com.rosato.modelo.Empleado;
import com.rosato.util.Navegador;
import com.rosato.util.PasswordUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EditarEmpleadoController {

    private static final String[] AREAS = {"Administrador", "Producción", "Ventas",
            "Decoración", "Entrega", "Limpieza"};

    private static Empleado seleccionado;
    public static void setSeleccionado(Empleado e) { seleccionado = e; }

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private TextField txtCedula;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private ChoiceBox<String> cbArea;
    @FXML private TextField txtExperiencia;
    @FXML private TextField txtDisponibilidad;
    @FXML private TextField txtSalario;
    @FXML private DatePicker dpContratacion;
    @FXML private DatePicker dpNacimiento;
    @FXML private DatePicker dpPrueba;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblMensaje;

    private final EmpleadoDAO dao = new EmpleadoDAO();

    @FXML
    private void initialize() {
        cbArea.setItems(FXCollections.observableArrayList(AREAS));
        if (seleccionado == null) {
            lblTitulo.setText("Nuevo empleado");
            cbArea.setValue("Producción");
            chkActivo.setSelected(true);
        } else {
            lblTitulo.setText("Editar empleado #" + seleccionado.getIdEmpleado());
            txtNombre.setText(seleccionado.getNombreCompleto());
            txtCedula.setText(seleccionado.getCedula());
            txtTelefono.setText(seleccionado.getTelefono());
            txtUsuario.setText(seleccionado.getUsuario());
            cbArea.setValue(seleccionado.getAreaTrabajo());
            txtExperiencia.setText(seleccionado.getExperiencia());
            txtDisponibilidad.setText(seleccionado.getDisponibilidad());
            if (seleccionado.getSalario() != null) txtSalario.setText(String.valueOf(seleccionado.getSalario()));
            dpContratacion.setValue(seleccionado.getFechaContratacion());
            dpNacimiento.setValue(seleccionado.getFechaNacimiento());
            dpPrueba.setValue(seleccionado.getFechaPruebaEmbarazo());
            chkActivo.setSelected(seleccionado.isActivo());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        String usuario = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String pass = txtContrasena.getText() == null ? "" : txtContrasena.getText();
        if (nombre.isEmpty() || usuario.isEmpty() || cbArea.getValue() == null) {
            mostrarError("Nombre, usuario y área son obligatorios."); return;
        }
        if (seleccionado == null && pass.isEmpty()) {
            mostrarError("La contraseña es obligatoria para un empleado nuevo."); return;
        }
        if (!pass.isEmpty() && !PasswordUtil.esFuerte(pass)) {
            mostrarError("La contraseña debe tener 8+ caracteres, 1 mayúscula y 1 número.");
            return;
        }
        Double salario = null;
        String s = txtSalario.getText() == null ? "" : txtSalario.getText().trim().replace(",", ".");
        if (!s.isEmpty()) {
            try { salario = Double.parseDouble(s); }
            catch (NumberFormatException ex) { mostrarError("Salario inválido."); return; }
            if (salario < 0) { mostrarError("Salario no puede ser negativo."); return; }
        }

        Empleado e = seleccionado == null ? new Empleado() : seleccionado;
        e.setNombreCompleto(nombre);
        e.setCedula(vacioANull(txtCedula.getText()));
        e.setTelefono(vacioANull(txtTelefono.getText()));
        e.setUsuario(usuario);
        if (!pass.isEmpty()) e.setContrasena(PasswordUtil.hash(pass));
        e.setAreaTrabajo(cbArea.getValue());
        e.setExperiencia(vacioANull(txtExperiencia.getText()));
        e.setDisponibilidad(vacioANull(txtDisponibilidad.getText()));
        e.setSalario(salario);
        e.setFechaContratacion(dpContratacion.getValue());
        e.setFechaNacimiento(dpNacimiento.getValue());
        e.setFechaPruebaEmbarazo(dpPrueba.getValue());
        e.setActivo(chkActivo.isSelected());

        try {
            if (seleccionado == null) dao.insertar(e);
            else                      dao.actualizar(e);
            seleccionado = null;
            Navegador.ir("ListadoEmpleados.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionado = null;
        try { Navegador.ir("ListadoEmpleados.fxml"); } catch (Exception ignored) { }
    }

    private static String vacioANull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
