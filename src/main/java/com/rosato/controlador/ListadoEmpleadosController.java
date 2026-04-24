package com.rosato.controlador;

import com.rosato.dao.EmpleadoDAO;
import com.rosato.modelo.Empleado;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;

public class ListadoEmpleadosController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] AREAS = {"Todas", "Administrador", "Producción", "Ventas",
            "Decoración", "Entrega", "Limpieza"};

    @FXML private TextField txtBuscar;
    @FXML private ChoiceBox<String> cbArea;
    @FXML private CheckBox chkSoloActivos;
    @FXML private TableView<Empleado> tblEmpleados;
    @FXML private TableColumn<Empleado, Integer> colId;
    @FXML private TableColumn<Empleado, String>  colNombre;
    @FXML private TableColumn<Empleado, String>  colUsuario;
    @FXML private TableColumn<Empleado, String>  colCedula;
    @FXML private TableColumn<Empleado, String>  colTelefono;
    @FXML private TableColumn<Empleado, String>  colArea;
    @FXML private TableColumn<Empleado, String>  colContrat;
    @FXML private TableColumn<Empleado, String>  colActivo;

    private final EmpleadoDAO dao = new EmpleadoDAO();

    @FXML
    private void initialize() {
        cbArea.setItems(FXCollections.observableArrayList(AREAS));
        cbArea.setValue("Todas");

        colId       .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdEmpleado()));
        colNombre   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCompleto()));
        colUsuario  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsuario()));
        colCedula   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getCedula()));
        colTelefono .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colArea     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAreaTrabajo()));
        colContrat  .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaContratacion() == null ? "" : d.getValue().getFechaContratacion().format(FMT)));
        colActivo   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActivo() ? "Sí" : "No"));

        cargar();
    }

    private void cargar() {
        try {
            tblEmpleados.setItems(FXCollections.observableArrayList(
                    dao.listar(txtBuscar.getText(), cbArea.getValue(), chkSoloActivos.isSelected())));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar los empleados: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(); }
    @FXML private void onRefrescar() { txtBuscar.clear(); cbArea.setValue("Todas"); chkSoloActivos.setSelected(true); cargar(); }

    @FXML
    private void onNuevo() {
        EditarEmpleadoController.setSeleccionado(null);
        try { Navegador.ir("EditarEmpleado.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEditar() {
        Empleado sel = tblEmpleados.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona un empleado para editar.").showAndWait();
            return;
        }
        EditarEmpleadoController.setSeleccionado(sel);
        try { Navegador.ir("EditarEmpleado.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
