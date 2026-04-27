package com.rosato.controlador;

import com.rosato.dao.CompraDAO;
import com.rosato.dao.IngredienteDAO;
import com.rosato.dao.ProveedorDAO;
import com.rosato.modelo.Compra;
import com.rosato.modelo.DetalleCompra;
import com.rosato.modelo.Ingrediente;
import com.rosato.modelo.Proveedor;
import com.rosato.util.Navegador;
import com.rosato.util.Sesion;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NuevaCompraController {

    @FXML private ComboBox<Proveedor> cbProveedor;
    @FXML private TextField txtFactura;
    @FXML private TextField txtObservaciones;

    @FXML private ComboBox<Ingrediente> cbIngrediente;
    @FXML private TextField txtCantidad;
    @FXML private TextField txtPrecio;

    @FXML private TableView<DetalleCompra> tblDetalle;
    @FXML private TableColumn<DetalleCompra, String>     colIng;
    @FXML private TableColumn<DetalleCompra, BigDecimal> colCant;
    @FXML private TableColumn<DetalleCompra, String>     colUni;
    @FXML private TableColumn<DetalleCompra, BigDecimal> colPrecio;
    @FXML private TableColumn<DetalleCompra, BigDecimal> colSubtotal;

    @FXML private Label lblTotal;
    @FXML private Label lblMensaje;

    private final ProveedorDAO proveedorDAO = new ProveedorDAO();
    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();
    private final CompraDAO compraDAO = new CompraDAO();

    private final ObservableList<DetalleCompra> detalles = FXCollections.observableArrayList();
    private final Map<Integer, Ingrediente> porId = new HashMap<>();

    @FXML
    private void initialize() {
        try {
            List<Proveedor> provs = proveedorDAO.listar("");
            cbProveedor.setItems(FXCollections.observableArrayList(provs));
            cbProveedor.setConverter(new StringConverter<>() {
                @Override public String toString(Proveedor p) { return p == null ? "" : p.toString(); }
                @Override public Proveedor fromString(String s) { return null; }
            });

            List<Ingrediente> ings = ingredienteDAO.listar("");
            cbIngrediente.setItems(FXCollections.observableArrayList(ings));
            cbIngrediente.setConverter(new StringConverter<>() {
                @Override public String toString(Ingrediente i) {
                    return i == null ? "" : i.getNombre() + " (" + i.getUnidad() + ")";
                }
                @Override public Ingrediente fromString(String s) { return null; }
            });
            ings.forEach(i -> porId.put(i.getIdIngrediente(), i));
        } catch (Exception ex) {
            mostrarError("No se pudieron cargar los catálogos: " + ex.getMessage());
        }

        colIng     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreIngrediente()));
        colCant    .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCantidad()));
        colUni     .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidad()));
        colPrecio  .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getPrecioUnitario()));
        colSubtotal.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getSubtotal()));
        tblDetalle.setItems(detalles);

        cbIngrediente.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            if (n != null && n.getCostoPromedio() != null && n.getCostoPromedio().signum() > 0) {
                txtPrecio.setText(n.getCostoPromedio().toPlainString());
            }
        });
    }

    @FXML
    private void onAgregarDetalle() {
        limpiarMensaje();
        Ingrediente ing = cbIngrediente.getValue();
        if (ing == null) { mostrarError("Selecciona un ingrediente."); return; }
        BigDecimal cant = parse(txtCantidad.getText(), "cantidad");
        BigDecimal precio = parse(txtPrecio.getText(), "precio unitario");
        if (cant == null || precio == null) return;
        if (cant.signum() <= 0 || precio.signum() < 0) {
            mostrarError("Cantidad > 0 y precio ≥ 0."); return;
        }
        DetalleCompra d = new DetalleCompra();
        d.setFkIdIngrediente(ing.getIdIngrediente());
        d.setNombreIngrediente(ing.getNombre());
        d.setUnidad(ing.getUnidad());
        d.setCantidad(cant);
        d.setPrecioUnitario(precio);
        d.setSubtotal(cant.multiply(precio).setScale(2, RoundingMode.HALF_UP));
        detalles.add(d);
        recalcularTotal();
        txtCantidad.clear();
        txtPrecio.clear();
    }

    @FXML
    private void onQuitarDetalle() {
        DetalleCompra sel = tblDetalle.getSelectionModel().getSelectedItem();
        if (sel != null) { detalles.remove(sel); recalcularTotal(); }
    }

    private void recalcularTotal() {
        BigDecimal total = detalles.stream()
                .map(DetalleCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText("RD$ " + total.setScale(2, RoundingMode.HALF_UP).toPlainString());
    }

    @FXML
    private void onRegistrar() {
        limpiarMensaje();
        Proveedor prov = cbProveedor.getValue();
        if (prov == null) { mostrarError("Selecciona un proveedor."); return; }
        if (detalles.isEmpty()) { mostrarError("Agrega al menos un ingrediente."); return; }
        try {
            Compra c = new Compra();
            c.setFkIdProveedor(prov.getIdProveedor());
            if (Sesion.esEmpleado()) c.setFkIdEmpleado(Sesion.getIdUsuario());
            c.setNumeroFactura(blankToNull(txtFactura.getText()));
            c.setObservaciones(blankToNull(txtObservaciones.getText()));
            compraDAO.registrar(c, detalles);
            new Alert(Alert.AlertType.INFORMATION,
                    "Compra #" + c.getIdCompra() + " registrada. Stock actualizado.")
                    .showAndWait();
            Navegador.ir("ListadoCompras.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo registrar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("ListadoCompras.fxml"); } catch (Exception ignored) { }
    }

    private BigDecimal parse(String s, String campo) {
        try {
            if (s == null || s.isBlank()) { mostrarError("Completa " + campo + "."); return null; }
            return new BigDecimal(s.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            mostrarError("Valor inválido en '" + campo + "'.");
            return null;
        }
    }

    private String blankToNull(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    private void mostrarError(String s) {
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
    private void limpiarMensaje() {
        lblMensaje.setVisible(false); lblMensaje.setManaged(false); lblMensaje.setText("");
    }
}
