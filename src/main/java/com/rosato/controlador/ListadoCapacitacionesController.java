package com.rosato.controlador;

import com.rosato.dao.CapacitacionDAO;
import com.rosato.modelo.Capacitacion;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ListadoCapacitacionesController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TableView<Capacitacion> tblCaps;
    @FXML private TableColumn<Capacitacion, Integer> colId;
    @FXML private TableColumn<Capacitacion, String>  colFecha;
    @FXML private TableColumn<Capacitacion, String>  colTitulo;
    @FXML private TableColumn<Capacitacion, String>  colInstructor;
    @FXML private TableColumn<Capacitacion, BigDecimal> colHoras;
    @FXML private TableColumn<Capacitacion, String>  colUbic;

    private final CapacitacionDAO dao = new CapacitacionDAO();

    @FXML
    private void initialize() {
        colId        .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdCapacitacion()));
        colFecha     .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFecha() == null ? "" : d.getValue().getFecha().format(FMT)));
        colTitulo    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitulo()));
        colInstructor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getInstructor()));
        colHoras     .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getHoras()));
        colUbic      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUbicacion()));
        cargar();
    }

    private void cargar() {
        try {
            tblCaps.setItems(FXCollections.observableArrayList(
                    dao.listar(dpDesde.getValue(), dpHasta.getValue())));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar las capacitaciones: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(); }
    @FXML private void onRefrescar() { dpDesde.setValue(null); dpHasta.setValue(null); cargar(); }

    @FXML
    private void onNueva() {
        EditarCapacitacionController.setSeleccionada(null);
        try { Navegador.ir("EditarCapacitacion.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEditar() {
        Capacitacion sel = tblCaps.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una capacitación para editar.").showAndWait();
            return;
        }
        EditarCapacitacionController.setSeleccionada(sel);
        try { Navegador.ir("EditarCapacitacion.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
