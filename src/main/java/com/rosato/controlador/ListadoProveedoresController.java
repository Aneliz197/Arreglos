package com.rosato.controlador;

import com.rosato.dao.ProveedorDAO;
import com.rosato.modelo.Proveedor;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ListadoProveedoresController {

    @FXML private TextField txtBuscar;
    @FXML private TableView<Proveedor> tblProveedores;
    @FXML private TableColumn<Proveedor, Integer> colId;
    @FXML private TableColumn<Proveedor, String>  colNombre;
    @FXML private TableColumn<Proveedor, String>  colRnc;
    @FXML private TableColumn<Proveedor, String>  colTelefono;
    @FXML private TableColumn<Proveedor, String>  colEmail;
    @FXML private TableColumn<Proveedor, String>  colDireccion;

    private final ProveedorDAO dao = new ProveedorDAO();

    @FXML
    private void initialize() {
        colId       .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdProveedor()));
        colNombre   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colRnc      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRnc()));
        colTelefono .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTelefono()));
        colEmail    .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colDireccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDireccion()));
        cargar("");
    }

    private void cargar(String filtro) {
        try {
            tblProveedores.setItems(FXCollections.observableArrayList(dao.listar(filtro)));
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(txtBuscar.getText()); }
    @FXML private void onRefrescar() { txtBuscar.clear(); cargar(""); }

    @FXML
    private void onNuevo() {
        EditarProveedorController.setSeleccionado(null);
        try { Navegador.ir("EditarProveedor.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEditar() {
        Proveedor sel = tblProveedores.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona un proveedor para editar.").showAndWait();
            return;
        }
        EditarProveedorController.setSeleccionado(sel);
        try { Navegador.ir("EditarProveedor.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
