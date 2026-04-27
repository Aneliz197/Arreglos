package com.rosato.controlador;

import com.rosato.dao.IngredienteDAO;
import com.rosato.modelo.Ingrediente;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.util.List;

public class ListadoIngredientesController {

    @FXML private TextField txtBuscar;
    @FXML private Label lblAlerta;
    @FXML private TableView<Ingrediente> tblIngredientes;
    @FXML private TableColumn<Ingrediente, Integer> colId;
    @FXML private TableColumn<Ingrediente, String> colNombre;
    @FXML private TableColumn<Ingrediente, String> colUnidad;
    @FXML private TableColumn<Ingrediente, BigDecimal> colStock;
    @FXML private TableColumn<Ingrediente, BigDecimal> colMinimo;
    @FXML private TableColumn<Ingrediente, BigDecimal> colCosto;
    @FXML private TableColumn<Ingrediente, String> colAlerta;

    private final IngredienteDAO dao = new IngredienteDAO();

    @FXML
    private void initialize() {
        colId     .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdIngrediente()));
        colNombre .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colUnidad .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidad()));
        colStock  .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStockActual()));
        colMinimo .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getStockMinimo()));
        colCosto  .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCostoPromedio()));
        colAlerta .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isBajoStock() ? "⚠️ Bajo stock" : "OK"));
        cargar("");
    }

    private void cargar(String filtro) {
        try {
            List<Ingrediente> lst = dao.listar(filtro);
            tblIngredientes.setItems(FXCollections.observableArrayList(lst));
            long bajos = lst.stream().filter(Ingrediente::isBajoStock).count();
            lblAlerta.setText(bajos > 0 ? ("⚠ " + bajos + " ingredientes por debajo del mínimo.") : "");
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudieron cargar los ingredientes: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onBuscar()    { cargar(txtBuscar.getText()); }
    @FXML private void onRefrescar() { txtBuscar.clear(); cargar(""); }

    @FXML
    private void onNuevo() {
        EditarIngredienteController.setSeleccionado(null);
        try { Navegador.ir("EditarIngrediente.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEditar() {
        Ingrediente sel = tblIngredientes.getSelectionModel().getSelectedItem();
        if (sel == null) {
            new Alert(Alert.AlertType.INFORMATION, "Selecciona un ingrediente para editar.").showAndWait();
            return;
        }
        EditarIngredienteController.setSeleccionado(sel);
        try { Navegador.ir("EditarIngrediente.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
