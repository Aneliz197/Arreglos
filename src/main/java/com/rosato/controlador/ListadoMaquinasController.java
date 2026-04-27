package com.rosato.controlador;

import com.rosato.dao.MaquinaDAO;
import com.rosato.modelo.Maquina;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListadoMaquinasController {

    private static final String[] ESTADOS = {"Todos", "Operativa", "En mantenimiento", "Fuera de servicio"};
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TextField txtBuscar;
    @FXML private ChoiceBox<String> cbEstado;
    @FXML private Label lblAlerta;
    @FXML private TableView<Maquina> tblMaquinas;
    @FXML private TableColumn<Maquina, String> colCodigo;
    @FXML private TableColumn<Maquina, String> colNombre;
    @FXML private TableColumn<Maquina, String> colTipo;
    @FXML private TableColumn<Maquina, String> colMarca;
    @FXML private TableColumn<Maquina, String> colUbic;
    @FXML private TableColumn<Maquina, String> colEstado;
    @FXML private TableColumn<Maquina, String> colProxRev;
    @FXML private TableColumn<Maquina, String> colAlerta;

    private final MaquinaDAO dao = new MaquinaDAO();

    @FXML
    private void initialize() {
        cbEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        cbEstado.setValue("Todos");

        colCodigo  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCodigo()));
        colNombre  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colTipo    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipo()));
        colMarca   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMarca()));
        colUbic    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUbicacion()));
        colEstado  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colProxRev .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getProximaRevision() == null ? "-" : d.getValue().getProximaRevision().format(FMT)));
        colAlerta  .setCellValueFactory(d -> new SimpleObjectProperty<>(alertaRevision(d.getValue().getProximaRevision())));
        cargar();
    }

    private static String alertaRevision(LocalDate proxima) {
        if (proxima == null) return "";
        LocalDate hoy = LocalDate.now();
        if (proxima.isBefore(hoy)) return "⚠ Vencida";
        if (!proxima.isAfter(hoy.plusDays(7))) return "⚠ Próxima";
        return "OK";
    }

    private void cargar() {
        try {
            List<Maquina> lst = dao.listar(txtBuscar.getText(), cbEstado.getValue());
            tblMaquinas.setItems(FXCollections.observableArrayList(lst));
            long vencidas = lst.stream()
                    .filter(m -> m.getProximaRevision() != null && m.getProximaRevision().isBefore(LocalDate.now()))
                    .count();
            lblAlerta.setText(vencidas > 0 ? ("⚠ " + vencidas + " máquinas con revisión vencida.") : "");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar las máquinas: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(); }
    @FXML private void onRefrescar() { txtBuscar.clear(); cbEstado.setValue("Todos"); cargar(); }

    @FXML
    private void onNueva() {
        EditarMaquinaController.setSeleccionada(null);
        try { Navegador.ir("EditarMaquina.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEditar() {
        Maquina sel = tblMaquinas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una máquina para editar.").showAndWait();
            return;
        }
        EditarMaquinaController.setSeleccionada(sel);
        try { Navegador.ir("EditarMaquina.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onMantenimientos() {
        Maquina sel = tblMaquinas.getSelectionModel().getSelectedItem();
        ListadoMantenimientosController.setFiltroMaquina(sel);
        try { Navegador.ir("ListadoMantenimientos.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
