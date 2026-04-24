package com.rosato.controlador;

import com.rosato.dao.ClienteDAO;
import com.rosato.modelo.Cliente;
import com.rosato.util.Navegador;
import com.rosato.util.PasswordUtil;
import com.rosato.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.regex.Pattern;

/**
 * Pantalla 1.2 - Registro de Cliente Nuevo.
 * Valida y persiste un cliente; al completarse inicia sesión automáticamente.
 */
public class RegistroController {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtEmail;
    @FXML private TextField txtRncCedula;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private PasswordField txtContrasena2;
    @FXML private CheckBox chkTerminos;
    @FXML private Label lblMensaje;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    private void onRegistrar() {
        limpiarMensaje();

        String nombre = txt(txtNombre);
        String apellido = txt(txtApellido);
        String direccion = txt(txtDireccion);
        String telefono = txt(txtTelefono).replaceAll("[^0-9+]", "");
        String email = txt(txtEmail);
        String rnc = txt(txtRncCedula);
        String usuario = txt(txtUsuario);
        String pass = txtContrasena.getText();
        String pass2 = txtContrasena2.getText();

        if (nombre.isBlank() || apellido.isBlank() || direccion.isBlank()
                || telefono.isBlank() || email.isBlank() || usuario.isBlank()
                || pass == null || pass.isBlank()) {
            mostrarError("Completa todos los campos obligatorios (*).");
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            mostrarError("Formato de email inválido.");
            return;
        }
        if (telefono.replaceAll("[^0-9]", "").length() < 10) {
            mostrarError("El teléfono debe tener al menos 10 dígitos.");
            return;
        }
        if (usuario.length() < 4) {
            mostrarError("El usuario debe tener al menos 4 caracteres.");
            return;
        }
        if (!PasswordUtil.esFuerte(pass)) {
            mostrarError("La contraseña debe tener 8+ caracteres, 1 mayúscula y 1 número.");
            return;
        }
        if (!pass.equals(pass2)) {
            mostrarError("Las contraseñas no coinciden.");
            return;
        }
        if (!chkTerminos.isSelected()) {
            mostrarError("Debes aceptar los términos y condiciones.");
            return;
        }

        try {
            if (clienteDAO.existeUsuario(usuario)) {
                mostrarError("El usuario ya está en uso.");
                return;
            }
            if (clienteDAO.existeEmail(email)) {
                mostrarError("Ya existe una cuenta con ese email.");
                return;
            }

            Cliente c = new Cliente();
            c.setNombre(nombre);
            c.setApellido(apellido);
            c.setDireccion(direccion);
            c.setTelefono(telefono);
            c.setEmail(email);
            c.setRncCedula(rnc.isBlank() ? null : rnc);
            c.setUsuario(usuario);
            c.setContrasena(PasswordUtil.hash(pass));
            c.setAceptaTerminos(true);
            int id = clienteDAO.insertar(c);

            // Auto-login y redirigir al dashboard del cliente.
            Sesion.iniciarCliente(id, c.getNombreCompleto());
            Navegador.ir("DashboardCliente.fxml");

        } catch (Exception ex) {
            mostrarError("Error al registrar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        try {
            Navegador.ir("Login.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo volver al login.");
        }
    }

    private String txt(TextField tf) {
        return tf.getText() == null ? "" : tf.getText().trim();
    }

    private void mostrarError(String msg) {
        lblMensaje.setText(msg);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void limpiarMensaje() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }
}
