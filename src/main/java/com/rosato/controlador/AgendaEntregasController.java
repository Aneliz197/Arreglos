package com.rosato.controlador;

import com.rosato.dao.EntregaDAO;
import com.rosato.modelo.Entrega;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AgendaEntregasController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] ESTADOS = {"Todos", "Pendiente", "En ruta", "Entregada", "Fallida"};

    @FXML private DatePicker dpFecha;
    @FXML private ChoiceBox<String> cbEstado;

    @FXML private TableView<Entrega> tblEntregas;
    @FXML private TableColumn<Entrega, Integer> colId;
    @FXML private TableColumn<Entrega, Integer> colPedido;
    @FXML private TableColumn<Entrega, String>  colHora;
    @FXML private TableColumn<Entrega, String>  colCliente;
    @FXML private TableColumn<Entrega, String>  colTipo;
    @FXML private TableColumn<Entrega, String>  colDireccion;
    @FXML private TableColumn<Entrega, String>  colEstado;
    @FXML private TableColumn<Entrega, BigDecimal> colSaldo;
    @FXML private TableColumn<Entrega, String>  colRepartidor;

    private final EntregaDAO entregaDAO = new EntregaDAO();

    @FXML
    private void initialize() {
        dpFecha.setValue(LocalDate.now());
        cbEstado.setItems(FXCollections.observableArrayList(ESTADOS));
        cbEstado.setValue("Todos");

        colId       .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdEntrega()));
        colPedido   .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getFkIdPedido()));
        colHora     .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaProgramada() == null ? "" : d.getValue().getFechaProgramada().format(HORA)));
        colCliente  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colTipo     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTipoEntrega()));
        colDireccion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDireccion()));
        colEstado   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));
        colSaldo    .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSaldoPendiente()));
        colRepartidor.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreRepartidor()));

        onCargar();
    }

    @FXML
    private void onCargar() {
        try {
            LocalDate f = dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue();
            tblEntregas.setItems(FXCollections.observableArrayList(
                    entregaDAO.listarPorFecha(f, cbEstado.getValue())));
        } catch (Exception ex) {
            error("No se pudo cargar la agenda: " + ex.getMessage());
        }
    }

    @FXML
    private void onAsignar() {
        Entrega sel = tblEntregas.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Selecciona una entrega."); return; }
        AsignarRepartidorController.setEntrega(sel);
        try { Navegador.ir("AsignarRepartidor.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onEnRuta() {
        Entrega sel = tblEntregas.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Selecciona una entrega."); return; }
        if (!"Pendiente".equals(sel.getEstado())) {
            info("Solo entregas 'Pendiente' pueden pasar a 'En ruta'."); return;
        }
        try {
            entregaDAO.cambiarEstado(sel.getIdEntrega(), "En ruta");
            onCargar();
        } catch (Exception ex) {
            error("No se pudo cambiar estado: " + ex.getMessage());
        }
    }

    @FXML
    private void onEntregar() {
        Entrega sel = tblEntregas.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Selecciona una entrega."); return; }
        if ("Entregada".equals(sel.getEstado()) || "Fallida".equals(sel.getEstado())) {
            info("Esta entrega ya está cerrada."); return;
        }
        EntregarYCobrarController.setEntrega(sel);
        try { Navegador.ir("EntregarYCobrar.fxml"); } catch (Exception ignored) { }
    }

    @FXML
    private void onFallida() {
        Entrega sel = tblEntregas.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Selecciona una entrega."); return; }
        Alert conf = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Marcar entrega #" + sel.getIdEntrega() + " como fallida?", ButtonType.OK, ButtonType.CANCEL);
        conf.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            try {
                entregaDAO.cambiarEstado(sel.getIdEntrega(), "Fallida");
                onCargar();
            } catch (Exception ex) {
                error("No se pudo marcar como fallida: " + ex.getMessage());
            }
        });
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }

    private void info(String s) { new Alert(Alert.AlertType.INFORMATION, s).showAndWait(); }
    private void error(String s) { new Alert(Alert.AlertType.ERROR, s).showAndWait(); }
}
