package com.rosato.controlador;

import com.rosato.dao.MaquinaDAO;
import com.rosato.modelo.Maquina;
import com.rosato.util.Navegador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class EditarMaquinaController {

    private static final String[] TIPOS = {"Horno", "Batidora", "Mezcladora", "Refrigerador",
            "Congelador", "Mesa de trabajo", "Utensilios", "Otro"};
    private static final String[] ESTADOS = {"Operativa", "En mantenimiento", "Fuera de servicio"};

    private static Maquina seleccionada;
    public static void setSeleccionada(Maquina m) { seleccionada = m; }

    @FXML private Label lblTitulo;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ChoiceBox<String> cbTipo;
    @FXML private TextField txtMarca;
    @FXML private TextField txtModelo;
    @FXML private TextField txtSerial;
    @FXML private DatePicker dpAdquisicion;
    @FXML private ChoiceBox<String> cbEstado;
    @FXML private DatePicker dpProxRev;
    @FXML private TextField txtUbicacion;
    @FXML private TextArea txtNotas;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblMensaje;

    private final MaquinaDAO dao = new MaquinaDAO();

    @FXML
    private void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(TIPOS));
        cbEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        if (seleccionada == null) {
            lblTitulo.setText("Nueva máquina");
            cbTipo.setValue("Horno");
            cbEstado.setValue("Operativa");
            chkActivo.setSelected(true);
        } else {
            lblTitulo.setText("Editar máquina #" + seleccionada.getIdMaquina());
            txtCodigo.setText(seleccionada.getCodigo());
            txtNombre.setText(seleccionada.getNombre());
            cbTipo.setValue(seleccionada.getTipo());
            txtMarca.setText(seleccionada.getMarca());
            txtModelo.setText(seleccionada.getModelo());
            txtSerial.setText(seleccionada.getSerial());
            dpAdquisicion.setValue(seleccionada.getFechaAdquisicion());
            cbEstado.setValue(seleccionada.getEstado());
            dpProxRev.setValue(seleccionada.getProximaRevision());
            txtUbicacion.setText(seleccionada.getUbicacion());
            txtNotas.setText(seleccionada.getNotas());
            chkActivo.setSelected(seleccionada.isActivo());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        String codigo = txtCodigo.getText() == null ? "" : txtCodigo.getText().trim();
        String nombre = txtNombre.getText() == null ? "" : txtNombre.getText().trim();
        if (codigo.isEmpty() || nombre.isEmpty() || cbTipo.getValue() == null) {
            mostrarError("Código, nombre y tipo son obligatorios."); return;
        }
        Maquina m = seleccionada == null ? new Maquina() : seleccionada;
        m.setCodigo(codigo);
        m.setNombre(nombre);
        m.setTipo(cbTipo.getValue());
        m.setMarca(vacioANull(txtMarca.getText()));
        m.setModelo(vacioANull(txtModelo.getText()));
        m.setSerial(vacioANull(txtSerial.getText()));
        m.setFechaAdquisicion(dpAdquisicion.getValue());
        m.setEstado(cbEstado.getValue());
        m.setProximaRevision(dpProxRev.getValue());
        m.setUbicacion(vacioANull(txtUbicacion.getText()));
        m.setNotas(vacioANull(txtNotas.getText()));
        m.setActivo(chkActivo.isSelected());
        try {
            if (seleccionada == null) dao.insertar(m);
            else                      dao.actualizar(m);
            seleccionada = null;
            Navegador.ir("ListadoMaquinas.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionada = null;
        try { Navegador.ir("ListadoMaquinas.fxml"); } catch (Exception ignored) { }
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
