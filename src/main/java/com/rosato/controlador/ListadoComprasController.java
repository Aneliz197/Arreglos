package com.rosato.controlador;

import com.rosato.dao.CompraDAO;
import com.rosato.modelo.Compra;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class ListadoComprasController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<Compra> tblCompras;
    @FXML private TableColumn<Compra, Integer> colId;
    @FXML private TableColumn<Compra, String>  colFecha;
    @FXML private TableColumn<Compra, String>  colProv;
    @FXML private TableColumn<Compra, String>  colFactura;
    @FXML private TableColumn<Compra, BigDecimal> colTotal;

    private final CompraDAO dao = new CompraDAO();

    @FXML
    private void initialize() {
        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdCompra()));
        colFecha.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaCompra() == null ? "" : d.getValue().getFechaCompra().format(FMT)));
        colProv.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreProveedor()));
        colFactura.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNumeroFactura()));
        colTotal.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getTotal()));
        cargar();
    }

    private void cargar() {
        try { tblCompras.setItems(FXCollections.observableArrayList(dao.listarTodas())); }
        catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Error: " + ex.getMessage()).showAndWait();
        }
    }

    @FXML private void onRefrescar() { cargar(); }

    @FXML
    private void onNueva() {
        try { Navegador.ir("NuevaCompra.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }
}
