package com.rosato.controlador;

import com.rosato.dao.PedidoDAO;
import com.rosato.modelo.Pedido;
import com.rosato.util.Navegador;
import com.rosato.util.Sesion;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/** Pantalla 2.4 (vista cliente): sus pedidos filtrados por su ID. */
public class DashboardClienteController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label lblBienvenida;
    @FXML private TableView<Pedido> tblPedidos;
    @FXML private TableColumn<Pedido, Integer> colId;
    @FXML private TableColumn<Pedido, String> colFechaPedido;
    @FXML private TableColumn<Pedido, String> colFechaEntrega;
    @FXML private TableColumn<Pedido, String> colEstado;
    @FXML private TableColumn<Pedido, BigDecimal> colTotal;
    @FXML private TableColumn<Pedido, BigDecimal> colSaldo;

    private final PedidoDAO pedidoDAO = new PedidoDAO();

    @FXML
    private void initialize() {
        String nombre = Sesion.getNombreMostrar() == null ? "" : Sesion.getNombreMostrar();
        lblBienvenida.setText("Bienvenida, " + nombre);

        colId.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdPedido()));
        colFechaPedido.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaPedido() == null ? "" : d.getValue().getFechaPedido().format(FMT)));
        colFechaEntrega.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaEntrega() == null ? "" : d.getValue().getFechaEntrega().format(FMT)));
        colEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colTotal.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSubtotal()));
        colSaldo.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSaldoPendiente()));

        cargar();
    }

    private void cargar() {
        try {
            Integer id = Sesion.getIdUsuario();
            if (id == null) {
                tblPedidos.setItems(FXCollections.observableArrayList());
                return;
            }
            tblPedidos.setItems(FXCollections.observableArrayList(pedidoDAO.listarPorCliente(id)));
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudieron cargar los pedidos: " + ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML private void onRefrescar() { cargar(); }

    @FXML
    private void onNuevoPedido() {
        try {
            Navegador.ir("NuevoPedido.fxml");
        } catch (Exception ex) {
            Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo abrir la pantalla: " + ex.getMessage());
            a.showAndWait();
        }
    }

    @FXML
    private void onCerrarSesion() {
        try {
            Sesion.cerrar();
            Navegador.ir("Login.fxml");
        } catch (Exception ignored) { }
    }
}
