package com.rosato.controlador;

import com.rosato.dao.RevisionHigieneDAO;
import com.rosato.modelo.RevisionHigiene;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ListadoRevisionesHigieneController {

    public static final String[] AREAS = {"Todas", "Producción", "Almacén", "Ventas",
            "Entrega", "Oficina", "General"};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private ChoiceBox<String> cbArea;
    @FXML private TableView<RevisionHigiene> tblRevisiones;
    @FXML private TableColumn<RevisionHigiene, Integer> colId;
    @FXML private TableColumn<RevisionHigiene, String>  colFecha;
    @FXML private TableColumn<RevisionHigiene, String>  colArea;
    @FXML private TableColumn<RevisionHigiene, String>  colResp;
    @FXML private TableColumn<RevisionHigiene, BigDecimal> colPct;
    @FXML private TableColumn<RevisionHigiene, String>  colEstado;
    @FXML private TableColumn<RevisionHigiene, String>  colObs;

    private final RevisionHigieneDAO dao = new RevisionHigieneDAO();

    @FXML
    private void initialize() {
        cbArea.setItems(FXCollections.observableArrayList(AREAS));
        cbArea.setValue("Todas");

        colId    .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdRevision()));
        colFecha .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFecha() == null ? "" : d.getValue().getFecha().format(FMT)));
        colArea  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getArea()));
        colResp  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getResponsable()));
        colPct   .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getPuntaje()));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colObs   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservaciones()));

        cargar();
    }

    private void cargar() {
        try {
            tblRevisiones.setItems(FXCollections.observableArrayList(
                    dao.listar(dpDesde.getValue(), dpHasta.getValue(), cbArea.getValue())));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar las revisiones: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(); }
    @FXML private void onRefrescar() { dpDesde.setValue(null); dpHasta.setValue(null); cbArea.setValue("Todas"); cargar(); }

    @FXML
    private void onNueva() {
        EditarRevisionHigieneController.setSeleccionada(null);
        try { Navegador.ir("EditarRevisionHigiene.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEditar() {
        RevisionHigiene sel = tblRevisiones.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una revisión para editar.").showAndWait();
            return;
        }
        try {
            EditarRevisionHigieneController.setSeleccionada(dao.porId(sel.getIdRevision()).orElse(sel));
            Navegador.ir("EditarRevisionHigiene.fxml");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "No se pudo abrir: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("PanelHigiene.fxml"); } catch (Exception ignored) { }
    }
}
