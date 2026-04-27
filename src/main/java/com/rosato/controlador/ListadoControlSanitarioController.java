package com.rosato.controlador;

import com.rosato.dao.ControlSanitarioDAO;
import com.rosato.dao.EmpleadoDAO;
import com.rosato.modelo.ControlSanitarioEmpleado;
import com.rosato.modelo.Empleado;
import com.rosato.util.HigieneCalc;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ListadoControlSanitarioController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ComboBox<Empleado> cbEmpleado;
    @FXML private TableView<ControlSanitarioEmpleado> tblControles;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colFecha;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colEmpleado;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colArea;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colUni;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colManos;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colRed;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colGuantes;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colUnas;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colSalud;
    @FXML private TableColumn<ControlSanitarioEmpleado, BigDecimal> colResumen;
    @FXML private TableColumn<ControlSanitarioEmpleado, String> colObs;

    private final ControlSanitarioDAO dao = new ControlSanitarioDAO();
    private final EmpleadoDAO empDao = new EmpleadoDAO();

    @FXML
    private void initialize() {
        colFecha   .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFecha() == null ? "" : d.getValue().getFecha().format(FMT)));
        colEmpleado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreEmpleado()));
        colArea    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAreaEmpleado()));
        colUni     .setCellValueFactory(d -> sino(d.getValue().isUniformeOk()));
        colManos   .setCellValueFactory(d -> sino(d.getValue().isManosOk()));
        colRed     .setCellValueFactory(d -> sino(d.getValue().isRedecillaOk()));
        colGuantes .setCellValueFactory(d -> sino(d.getValue().isGuantesOk()));
        colUnas    .setCellValueFactory(d -> sino(d.getValue().isUnasOk()));
        colSalud   .setCellValueFactory(d -> sino(d.getValue().isSaludOk()));
        colResumen .setCellValueFactory(d -> new SimpleObjectProperty<>(
                HigieneCalc.porcentaje(d.getValue().aprobadas(), d.getValue().totalChecks())));
        colObs     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservaciones()));

        cargarEmpleados();
        cargar();
    }

    private SimpleStringProperty sino(boolean b) {
        return new SimpleStringProperty(b ? "Sí" : "No");
    }

    private void cargarEmpleados() {
        try {
            cbEmpleado.setItems(FXCollections.observableArrayList(empDao.listarActivos()));
            cbEmpleado.setConverter(new StringConverter<>() {
                @Override public String toString(Empleado e) {
                    return e == null ? "" : e.getNombreCompleto();
                }
                @Override public Empleado fromString(String s) { return null; }
            });
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar empleados: " + ex.getMessage()).showAndWait();
        }
    }

    private void cargar() {
        try {
            Integer idEmp = cbEmpleado.getValue() == null ? null : cbEmpleado.getValue().getIdEmpleado();
            tblControles.setItems(FXCollections.observableArrayList(
                    dao.listar(dpDesde.getValue(), dpHasta.getValue(), idEmp)));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar los controles: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()     { cargar(); }
    @FXML private void onLimpiarEmp() { cbEmpleado.setValue(null); cargar(); }
    @FXML private void onRefrescar()  { dpDesde.setValue(null); dpHasta.setValue(null); cbEmpleado.setValue(null); cargar(); }

    @FXML
    private void onNuevo() {
        try { Navegador.ir("NuevoControlSanitario.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("PanelHigiene.fxml"); } catch (Exception ignored) { }
    }
}
