package com.rosato.controlador;

import com.rosato.dao.CapacitacionDAO;
import com.rosato.dao.EmpleadoDAO;
import com.rosato.modelo.Capacitacion;
import com.rosato.modelo.CapacitacionEmpleado;
import com.rosato.modelo.Empleado;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.BigDecimalStringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;

public class EditarCapacitacionController {

    private static Capacitacion seleccionada;
    public static void setSeleccionada(Capacitacion c) { seleccionada = c; }

    @FXML private Label lblTitulo;
    @FXML private TextField txtTitulo;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtInstructor;
    @FXML private TextField txtHoras;
    @FXML private TextField txtUbicacion;
    @FXML private TextArea txtDescripcion;
    @FXML private Label lblMensaje;

    @FXML private VBox panelAsistentes;
    @FXML private ComboBox<Empleado> cbEmpleado;
    @FXML private TableView<CapacitacionEmpleado> tblAsistentes;
    @FXML private TableColumn<CapacitacionEmpleado, String>  colEmpleado;
    @FXML private TableColumn<CapacitacionEmpleado, String>  colArea;
    @FXML private TableColumn<CapacitacionEmpleado, Boolean> colAsistio;
    @FXML private TableColumn<CapacitacionEmpleado, BigDecimal> colCalif;
    @FXML private TableColumn<CapacitacionEmpleado, String>  colObs;

    private final CapacitacionDAO dao = new CapacitacionDAO();
    private final EmpleadoDAO empDao = new EmpleadoDAO();

    @FXML
    private void initialize() {
        dpFecha.setValue(LocalDate.now());
        txtHoras.setText("0");
        if (seleccionada == null) {
            lblTitulo.setText("Nueva capacitación");
            panelAsistentes.setVisible(false);
            panelAsistentes.setManaged(false);
        } else {
            lblTitulo.setText("Capacitación #" + seleccionada.getIdCapacitacion());
            txtTitulo.setText(seleccionada.getTitulo());
            dpFecha.setValue(seleccionada.getFecha());
            txtInstructor.setText(seleccionada.getInstructor());
            txtHoras.setText(seleccionada.getHoras() == null ? "0" : seleccionada.getHoras().toPlainString());
            txtUbicacion.setText(seleccionada.getUbicacion());
            txtDescripcion.setText(seleccionada.getDescripcion());
            configurarTablaAsistentes();
            cargarEmpleados();
            cargarAsistentes();
        }
    }

    private void configurarTablaAsistentes() {
        colEmpleado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreEmpleado()));
        colArea    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAreaEmpleado()));
        colAsistio .setCellValueFactory(d -> {
            SimpleBooleanProperty p = new SimpleBooleanProperty(d.getValue().isAsistio());
            p.addListener((obs, oldV, newV) -> d.getValue().setAsistio(newV));
            return p;
        });
        colAsistio.setCellFactory(CheckBoxTableCell.forTableColumn(colAsistio));
        colAsistio.setEditable(true);

        colCalif.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCalificacion()));
        colCalif.setCellFactory(TextFieldTableCell.forTableColumn(new BigDecimalStringConverter()));
        colCalif.setOnEditCommit(ev -> ev.getRowValue().setCalificacion(ev.getNewValue()));
        colCalif.setEditable(true);

        colObs.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservaciones()));
        colObs.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        colObs.setOnEditCommit(ev -> ev.getRowValue().setObservaciones(ev.getNewValue()));
        colObs.setEditable(true);
    }

    private void cargarEmpleados() {
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

    private void cargarAsistentes() {
        if (seleccionada == null || seleccionada.getIdCapacitacion() == null) return;
        try {
            tblAsistentes.setItems(FXCollections.observableArrayList(
                    dao.listarAsistentes(seleccionada.getIdCapacitacion())));
        } catch (Exception ex) {
            mostrarError("No se pudieron cargar asistentes: " + ex.getMessage());
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        String titulo = txtTitulo.getText() == null ? "" : txtTitulo.getText().trim();
        if (titulo.isEmpty() || dpFecha.getValue() == null) {
            mostrarError("Título y fecha son obligatorios."); return;
        }
        BigDecimal horas;
        try {
            String h = txtHoras.getText() == null ? "0" : txtHoras.getText().trim().replace(",", ".");
            if (h.isEmpty()) h = "0";
            horas = new BigDecimal(h);
            if (horas.signum() < 0) { mostrarError("Las horas no pueden ser negativas."); return; }
        } catch (NumberFormatException ex) {
            mostrarError("Horas inválidas."); return;
        }
        Capacitacion c = seleccionada == null ? new Capacitacion() : seleccionada;
        c.setTitulo(titulo);
        c.setFecha(dpFecha.getValue());
        c.setInstructor(vacioANull(txtInstructor.getText()));
        c.setHoras(horas);
        c.setUbicacion(vacioANull(txtUbicacion.getText()));
        c.setDescripcion(vacioANull(txtDescripcion.getText()));
        try {
            if (seleccionada == null) {
                dao.insertar(c);
                seleccionada = c;
                lblTitulo.setText("Capacitación #" + c.getIdCapacitacion());
                panelAsistentes.setVisible(true);
                panelAsistentes.setManaged(true);
                configurarTablaAsistentes();
                cargarEmpleados();
                cargarAsistentes();
            } else {
                dao.actualizar(c);
            }
            mostrarOk("Información guardada.");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onAgregarAsistente() {
        if (seleccionada == null || seleccionada.getIdCapacitacion() == null) {
            mostrarError("Primero guarda la información de la capacitación."); return;
        }
        Empleado e = cbEmpleado.getValue();
        if (e == null) { mostrarError("Selecciona un empleado."); return; }
        try {
            dao.agregarAsistente(seleccionada.getIdCapacitacion(), e.getIdEmpleado());
            cargarAsistentes();
        } catch (Exception ex) {
            mostrarError("No se pudo agregar: " + ex.getMessage());
        }
    }

    @FXML
    private void onQuitarAsistente() {
        CapacitacionEmpleado sel = tblAsistentes.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            dao.quitarAsistente(sel.getIdCapacitacionEmp());
            cargarAsistentes();
        } catch (Exception ex) {
            mostrarError("No se pudo quitar: " + ex.getMessage());
        }
    }

    @FXML
    private void onGuardarAsistencias() {
        try {
            for (CapacitacionEmpleado ce : tblAsistentes.getItems()) {
                dao.actualizarAsistente(ce);
            }
            mostrarOk("Asistencias guardadas.");
        } catch (Exception ex) {
            mostrarError("No se pudieron guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionada = null;
        try { Navegador.ir("ListadoCapacitaciones.fxml"); } catch (Exception ignored) { }
    }

    private static String vacioANull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private void mostrarError(String s) {
        lblMensaje.getStyleClass().removeAll("ok");
        if (!lblMensaje.getStyleClass().contains("error")) lblMensaje.getStyleClass().add("error");
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
    private void mostrarOk(String s) {
        lblMensaje.getStyleClass().removeAll("error");
        if (!lblMensaje.getStyleClass().contains("ok")) lblMensaje.getStyleClass().add("ok");
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
    private void limpiarMensaje() {
        lblMensaje.setVisible(false); lblMensaje.setManaged(false); lblMensaje.setText("");
    }
}
