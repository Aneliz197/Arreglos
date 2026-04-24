package com.rosato.controlador;

import com.rosato.dao.IngredienteDAO;
import com.rosato.modelo.Ingrediente;
import com.rosato.util.Navegador;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.math.BigDecimal;

public class EditarIngredienteController {

    private static Ingrediente seleccionado;
    public static void setSeleccionado(Ingrediente i) { seleccionado = i; }

    private static final String[] UNIDADES = {"lb", "kg", "g", "L", "ml", "unidad", "docena"};

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombre;
    @FXML private ChoiceBox<String> cbUnidad;
    @FXML private TextField txtStock;
    @FXML private TextField txtMinimo;
    @FXML private TextField txtCosto;
    @FXML private CheckBox chkActivo;
    @FXML private Label lblMensaje;

    private final IngredienteDAO dao = new IngredienteDAO();

    @FXML
    private void initialize() {
        cbUnidad.setItems(FXCollections.observableArrayList(UNIDADES));
        cbUnidad.getSelectionModel().selectFirst();

        if (seleccionado != null) {
            lblTitulo.setText("Editar ingrediente #" + seleccionado.getIdIngrediente());
            txtNombre.setText(seleccionado.getNombre());
            cbUnidad.setValue(seleccionado.getUnidad());
            txtStock.setText(txt(seleccionado.getStockActual()));
            txtMinimo.setText(txt(seleccionado.getStockMinimo()));
            txtCosto.setText(txt(seleccionado.getCostoPromedio()));
            chkActivo.setSelected(seleccionado.isActivo());
        } else {
            txtStock.setText("0");
            txtMinimo.setText("0");
            txtCosto.setText("0");
        }
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mostrarError("Nombre obligatorio."); return;
        }
        BigDecimal stock = parse(txtStock.getText(), "stock actual");
        BigDecimal minimo = parse(txtMinimo.getText(), "stock mínimo");
        BigDecimal costo = parse(txtCosto.getText(), "costo promedio");
        if (stock == null || minimo == null || costo == null) return;
        if (stock.signum() < 0 || minimo.signum() < 0 || costo.signum() < 0) {
            mostrarError("Los valores numéricos no pueden ser negativos."); return;
        }
        try {
            Ingrediente i = seleccionado == null ? new Ingrediente() : seleccionado;
            i.setNombre(txtNombre.getText().trim());
            i.setUnidad(cbUnidad.getValue());
            i.setStockActual(stock);
            i.setStockMinimo(minimo);
            i.setCostoPromedio(costo);
            i.setActivo(chkActivo.isSelected());
            if (i.getIdIngrediente() == null) dao.insertar(i);
            else dao.actualizar(i);
            seleccionado = null;
            Navegador.ir("ListadoIngredientes.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionado = null;
        try { Navegador.ir("ListadoIngredientes.fxml"); } catch (Exception ignored) { }
    }

    private BigDecimal parse(String s, String campo) {
        try {
            if (s == null || s.isBlank()) return BigDecimal.ZERO;
            return new BigDecimal(s.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            mostrarError("Valor inválido en '" + campo + "'.");
            return null;
        }
    }

    private String txt(BigDecimal v) { return v == null ? "0" : v.toPlainString(); }

    private void mostrarError(String s) {
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
    private void limpiarMensaje() {
        lblMensaje.setText("");
        lblMensaje.setVisible(false);
        lblMensaje.setManaged(false);
    }
}
