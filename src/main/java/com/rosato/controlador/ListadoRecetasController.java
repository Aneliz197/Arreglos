package com.rosato.controlador;

import com.rosato.dao.RecetaDAO;
import com.rosato.modelo.Receta;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ListadoRecetasController {

    @FXML private TableView<Receta> tblRecetas;
    @FXML private TableColumn<Receta, Integer> colId;
    @FXML private TableColumn<Receta, String>  colTipo;
    @FXML private TableColumn<Receta, String>  colNombre;
    @FXML private TableColumn<Receta, String>  colNotas;
    @FXML private TableColumn<Receta, Boolean> colActiva;

    private final RecetaDAO dao = new RecetaDAO();

    @FXML
    private void initialize() {
        colId    .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdReceta()));
        colTipo  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoProducto()));
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colNotas .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotas()));
        colActiva.setCellValueFactory(d -> new SimpleBooleanProperty(d.getValue().isActiva()));
        cargar();
    }

    private void cargar() {
        try { tblRecetas.setItems(FXCollections.observableArrayList(dao.listar())); }
        catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onRefrescar() { cargar(); }

    @FXML
    private void onNuevo() {
        EditarRecetaController.setSeleccionado(null);
        try { Navegador.ir("EditarReceta.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEditar() {
        Receta sel = tblRecetas.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona una receta para editar.").showAndWait();
            return;
        }
        EditarRecetaController.setSeleccionado(sel);
        try { Navegador.ir("EditarReceta.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
