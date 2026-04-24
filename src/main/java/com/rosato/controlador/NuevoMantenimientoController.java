package com.rosato.controlador;

import com.rosato.dao.MantenimientoDAO;
import com.rosato.dao.MaquinaDAO;
import com.rosato.modelo.Mantenimiento;
import com.rosato.modelo.Maquina;
import com.rosato.util.Navegador;
import com.rosato.util.Sesion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class NuevoMantenimientoController {

    private static final String[] TIPOS = {"Preventivo", "Correctivo"};

    private static Maquina maquinaInicial;
    public static void setMaquinaInicial(Maquina m) { maquinaInicial = m; }

    @FXML private ComboBox<Maquina> cbMaquina;
    @FXML private ChoiceBox<String> cbTipo;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtTecnico;
    @FXML private TextField txtCosto;
    @FXML private TextArea txtDescripcion;
    @FXML private DatePicker dpProxRev;
    @FXML private Label lblMensaje;

    private final MaquinaDAO maquinaDAO = new MaquinaDAO();
    private final MantenimientoDAO dao = new MantenimientoDAO();

    @FXML
    private void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(TIPOS));
        cbTipo.setValue("Preventivo");
        dpFecha.setValue(LocalDate.now());
        txtCosto.setText("0");
        try {
            cbMaquina.setItems(FXCollections.observableArrayList(maquinaDAO.listarActivas()));
            cbMaquina.setConverter(new StringConverter<>() {
                @Override public String toString(Maquina m) {
                    return m == null ? "" : m.getCodigo() + " · " + m.getNombre();
                }
                @Override public Maquina fromString(String s) { return null; }
            });
            if (maquinaInicial != null) {
                cbMaquina.getItems().stream()
                        .filter(m -> m.getIdMaquina().equals(maquinaInicial.getIdMaquina()))
                        .findFirst().ifPresent(cbMaquina::setValue);
            }
        } catch (Exception ex) {
            mostrarError("No se pudo cargar máquinas: " + ex.getMessage());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        Maquina maq = cbMaquina.getValue();
        if (maq == null) { mostrarError("Selecciona una máquina."); return; }
        if (cbTipo.getValue() == null) { mostrarError("Selecciona tipo."); return; }
        if (dpFecha.getValue() == null) { mostrarError("Indica la fecha."); return; }

        BigDecimal costo;
        try {
            String t = txtCosto.getText() == null ? "0" : txtCosto.getText().trim().replace(",", ".");
            if (t.isEmpty()) t = "0";
            costo = new BigDecimal(t);
            if (costo.signum() < 0) { mostrarError("El costo no puede ser negativo."); return; }
        } catch (NumberFormatException ex) {
            mostrarError("Costo inválido."); return;
        }

        LocalDate fecha = dpFecha.getValue();
        LocalDate prox = dpProxRev.getValue();
        if (prox != null && prox.isBefore(fecha)) {
            mostrarError("La próxima revisión no puede ser anterior a la fecha del mantenimiento.");
            return;
        }

        Mantenimiento m = new Mantenimiento();
        m.setFkIdMaquina(maq.getIdMaquina());
        m.setTipo(cbTipo.getValue());
        m.setFecha(fecha);
        m.setTecnico(vacioANull(txtTecnico.getText()));
        m.setCosto(costo);
        m.setDescripcion(vacioANull(txtDescripcion.getText()));
        m.setProximaRevision(prox);
        if (Sesion.esEmpleado()) m.setFkIdEmpleado(Sesion.getIdUsuario());

        try {
            dao.registrar(m);
            maquinaInicial = null;
            Navegador.ir("ListadoMantenimientos.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo registrar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        maquinaInicial = null;
        try { Navegador.ir("ListadoMantenimientos.fxml"); } catch (Exception ignored) { }
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
