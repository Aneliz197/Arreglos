package com.rosato.controlador;

import com.rosato.dao.MantenimientoDAO;
import com.rosato.modelo.Mantenimiento;
import com.rosato.modelo.Maquina;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ListadoMantenimientosController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static Maquina filtroMaquina;
    public static void setFiltroMaquina(Maquina m) { filtroMaquina = m; }

    @FXML private Label lblTitulo;
    @FXML private Label lblFiltroMaquina;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TableView<Mantenimiento> tblMants;
    @FXML private TableColumn<Mantenimiento, Integer> colId;
    @FXML private TableColumn<Mantenimiento, String>  colFecha;
    @FXML private TableColumn<Mantenimiento, String>  colMaquina;
    @FXML private TableColumn<Mantenimiento, String>  colTipo;
    @FXML private TableColumn<Mantenimiento, String>  colTecnico;
    @FXML private TableColumn<Mantenimiento, BigDecimal> colCosto;
    @FXML private TableColumn<Mantenimiento, String>  colProxRev;
    @FXML private TableColumn<Mantenimiento, String>  colDesc;

    private final MantenimientoDAO dao = new MantenimientoDAO();

    @FXML
    private void initialize() {
        colId      .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdMantenimiento()));
        colFecha   .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFecha() == null ? "" : d.getValue().getFecha().format(FMT)));
        colMaquina .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getCodigoMaquina() + " · " + d.getValue().getNombreMaquina()));
        colTipo    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colTecnico .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTecnico()));
        colCosto   .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCosto()));
        colProxRev .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getProximaRevision() == null ? "-" : d.getValue().getProximaRevision().format(FMT)));
        colDesc    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));

        if (filtroMaquina != null) {
            lblTitulo.setText("Mantenimientos · " + filtroMaquina.getCodigo() + " " + filtroMaquina.getNombre());
            lblFiltroMaquina.setText("Filtrado por máquina: " + filtroMaquina.getCodigo());
        } else {
            lblFiltroMaquina.setText("");
        }
        cargar();
    }

    private void cargar() {
        try {
            Integer idMaq = filtroMaquina == null ? null : filtroMaquina.getIdMaquina();
            tblMants.setItems(FXCollections.observableArrayList(
                    dao.listar(idMaq, dpDesde.getValue(), dpHasta.getValue())));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar los mantenimientos: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(); }

    @FXML
    private void onLimpiar() {
        filtroMaquina = null;
        lblFiltroMaquina.setText("");
        dpDesde.setValue(null); dpHasta.setValue(null);
        lblTitulo.setText("Historial de mantenimientos");
        cargar();
    }

    @FXML
    private void onRefrescar() { cargar(); }

    @FXML
    private void onNuevo() {
        NuevoMantenimientoController.setMaquinaInicial(filtroMaquina);
        try { Navegador.ir("NuevoMantenimiento.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        filtroMaquina = null;
        try { Navegador.ir("ListadoMaquinas.fxml"); } catch (Exception ignored) { }
    }
}
