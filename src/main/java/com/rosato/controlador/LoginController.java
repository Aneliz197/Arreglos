package com.rosato.controlador;

import com.rosato.dao.ClienteDAO;
import com.rosato.dao.ConexionBD;
import com.rosato.dao.EmpleadoDAO;
import com.rosato.modelo.Cliente;
import com.rosato.modelo.Empleado;
import com.rosato.util.Navegador;
import com.rosato.util.PasswordUtil;
import com.rosato.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Pantalla 1.1 - Inicio de Sesión.
 * Valida contra Cliente y Empleado, implementa bloqueo tras 3 intentos fallidos.
 */
public class LoginController {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtContrasena;
    @FXML private CheckBox chkRecordarme;
    @FXML private Label lblMensaje;
    @FXML private Label lblRolDetectado;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    @FXML
    private void onIngresar() {
        limpiarMensaje();
        String usuario = txtUsuario.getText() == null ? "" : txtUsuario.getText().trim();
        String pass = txtContrasena.getText();

        if (usuario.isBlank() || pass == null || pass.isBlank()) {
            mostrarError("Completa usuario y contraseña.");
            return;
        }

        try {
            // 1) Intentar como empleado primero (el admin es empleado)
            var emp = empleadoDAO.porUsuario(usuario);
            if (emp.isPresent()) {
                Empleado e = emp.get();
                if (bloqueado(e.getBloqueadoHasta())) {
                    mostrarError("Cuenta bloqueada temporalmente. Inténtalo más tarde.");
                    return;
                }
                if (PasswordUtil.verificar(pass, e.getContrasena())) {
                    empleadoDAO.reiniciarIntentos(e.getIdEmpleado());
                    Sesion.iniciarEmpleado(e.getIdEmpleado(), e.getNombreCompleto(), e.getAreaTrabajo());
                    Navegador.ir("DashboardEmpleado.fxml");
                    return;
                } else {
                    manejarFalloEmpleado(e);
                    return;
                }
            }

            // 2) Intentar como cliente
            var cli = clienteDAO.porUsuario(usuario);
            if (cli.isPresent()) {
                Cliente c = cli.get();
                if (bloqueado(c.getBloqueadoHasta())) {
                    mostrarError("Cuenta bloqueada temporalmente. Inténtalo más tarde.");
                    return;
                }
                if (PasswordUtil.verificar(pass, c.getContrasena())) {
                    clienteDAO.reiniciarIntentos(c.getIdCliente());
                    Sesion.iniciarCliente(c.getIdCliente(), c.getNombreCompleto());
                    Navegador.ir("DashboardCliente.fxml");
                    return;
                } else {
                    manejarFalloCliente(c);
                    return;
                }
            }

            // Usuario no existe (mensaje genérico por seguridad).
            mostrarError("Usuario o contraseña incorrectos.");

        } catch (SQLException ex) {
            mostrarError("Error al conectar a la base de datos: " + ex.getMessage());
        } catch (Exception ex) {
            mostrarError("Error inesperado: " + ex.getMessage());
        }
    }

    @FXML
    private void onRegistro() {
        try {
            Navegador.ir("Registro.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo abrir el formulario de registro.");
        }
    }

    @FXML
    private void onOlvidoPassword() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Recuperar contraseña");
        a.setHeaderText(null);
        a.setContentText("Funcionalidad pendiente. Contacta a un administrador para restablecer tu contraseña.");
        a.showAndWait();
    }

    // ----------------- helpers -----------------

    private void manejarFalloCliente(Cliente c) throws SQLException {
        clienteDAO.registrarIntentoFallido(c.getIdCliente());
        int intentos = c.getIntentosFallidos() + 1;
        int max = ConexionBD.getIntProp("auth.max.intentos", 3);
        int minutos = ConexionBD.getIntProp("auth.bloqueo.minutos", 15);
        if (intentos >= max) {
            clienteDAO.bloquearHasta(c.getIdCliente(), LocalDateTime.now().plusMinutes(minutos));
            mostrarError("Demasiados intentos. Cuenta bloqueada por " + minutos + " minutos.");
        } else {
            mostrarError("Usuario o contraseña incorrectos. (" + intentos + "/" + max + ")");
        }
    }

    private void manejarFalloEmpleado(Empleado e) throws SQLException {
        empleadoDAO.registrarIntentoFallido(e.getIdEmpleado());
        int intentos = e.getIntentosFallidos() + 1;
        int max = ConexionBD.getIntProp("auth.max.intentos", 3);
        int minutos = ConexionBD.getIntProp("auth.bloqueo.minutos", 15);
        if (intentos >= max) {
            empleadoDAO.bloquearHasta(e.getIdEmpleado(), LocalDateTime.now().plusMinutes(minutos));
            mostrarError("Demasiados intentos. Cuenta bloqueada por " + minutos + " minutos.");
        } else {
            mostrarError("Usuario o contraseña incorrectos. (" + intentos + "/" + max + ")");
        }
    }

    private boolean bloqueado(LocalDateTime hasta) {
        return hasta != null && hasta.isAfter(LocalDateTime.now());
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
