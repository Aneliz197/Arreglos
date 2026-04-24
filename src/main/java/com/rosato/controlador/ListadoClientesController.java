package com.rosato.controlador;

import com.rosato.dao.ClienteDAO;
import com.rosato.modelo.Cliente;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.format.DateTimeFormatter;

/** Pantalla 2.1 (base): listado de clientes registrados. */
public class ListadoClientesController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField txtBuscar;
    @FXML private TableView<Cliente> tblClientes;
    @FXML private TableColumn<Cliente, Integer> colId;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colApellido;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colFecha;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdCliente()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colApellido.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getApellido()));
        colTelefono.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaRegistro() == null ? "" : d.getValue().getFechaRegistro().format(FMT)));
        cargar("");
    }

    private void cargar(String filtro) {
        try {
            tblClientes.setItems(FXCollections.observableArrayList(clienteDAO.listar(filtro)));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar los clientes: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(txtBuscar.getText()); }
    @FXML private void onRefrescar() { txtBuscar.clear(); cargar(""); }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
