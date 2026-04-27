package com.rosato.controlador;

import com.rosato.dao.IngredienteDAO;
import com.rosato.dao.RecetaDAO;
import com.rosato.modelo.Ingrediente;
import com.rosato.modelo.Receta;
import com.rosato.modelo.RecetaIngrediente;
import com.rosato.util.Navegador;
import com.rosato.util.PrecioService;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditarRecetaController {

    private static Receta seleccionada;
    public static void setSeleccionado(Receta r) { seleccionada = r; }

    @FXML private Label lblTitulo;
    @FXML private ChoiceBox<String> cbTipo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtNotas;
    @FXML private CheckBox chkActiva;

    @FXML private ComboBox<Ingrediente> cbIngrediente;
    @FXML private TextField txtCantidad;

    @FXML private TableView<RecetaIngrediente> tblIngr;
    @FXML private TableColumn<RecetaIngrediente, String>     colIng;
    @FXML private TableColumn<RecetaIngrediente, String>     colUni;
    @FXML private TableColumn<RecetaIngrediente, BigDecimal> colCant;

    @FXML private Label lblMensaje;

    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();
    private final ObservableList<RecetaIngrediente> items = FXCollections.observableArrayList();
    private final Map<Integer, Ingrediente> porId = new HashMap<>();

    @FXML
    private void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(PrecioService.tiposProducto()));

        try {
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
            mostrarError("No se pudieron cargar ingredientes: " + ex.getMessage());
        }

        colIng .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreIngrediente()));
        colUni .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidad()));
        colCant.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getCantidadPorLibra()));
        tblIngr.setItems(items);

        if (seleccionada != null) {
            lblTitulo.setText("Editar receta #" + seleccionada.getIdReceta());
            cbTipo.setValue(seleccionada.getTipoProducto());
            txtNombre.setText(seleccionada.getNombre());
            txtNotas.setText(seleccionada.getNotas());
            chkActiva.setSelected(seleccionada.isActiva());
            items.addAll(seleccionada.getIngredientes());
        } else {
            cbTipo.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void onAgregar() {
        limpiarMensaje();
        Ingrediente ing = cbIngrediente.getValue();
        if (ing == null) { mostrarError("Selecciona un ingrediente."); return; }
        if (items.stream().anyMatch(r -> r.getFkIdIngrediente() == ing.getIdIngrediente())) {
            mostrarError("Ese ingrediente ya está en la receta."); return;
        }
        BigDecimal cant;
        try {
            if (txtCantidad.getText() == null || txtCantidad.getText().isBlank()) {
                mostrarError("Indica la cantidad por libra."); return;
            }
            cant = new BigDecimal(txtCantidad.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            mostrarError("Cantidad inválida."); return;
        }
        if (cant.signum() <= 0) { mostrarError("Cantidad debe ser > 0."); return; }

        RecetaIngrediente ri = new RecetaIngrediente();
        ri.setFkIdIngrediente(ing.getIdIngrediente());
        ri.setNombreIngrediente(ing.getNombre());
        ri.setUnidad(ing.getUnidad());
        ri.setCantidadPorLibra(cant);
        items.add(ri);
        txtCantidad.clear();
    }

    @FXML
    private void onQuitar() {
        RecetaIngrediente sel = tblIngr.getSelectionModel().getSelectedItem();
        if (sel != null) items.remove(sel);
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        if (cbTipo.getValue() == null) { mostrarError("Selecciona un tipo de producto."); return; }
        if (txtNombre.getText() == null || txtNombre.getText().isBlank()) {
            mostrarError("Nombre obligatorio."); return;
        }
        if (items.isEmpty()) { mostrarError("Agrega al menos un ingrediente."); return; }
        try {
            Receta r = seleccionada == null ? new Receta() : seleccionada;
            r.setTipoProducto(cbTipo.getValue());
            r.setNombre(txtNombre.getText().trim());
            r.setNotas(txtNotas.getText() == null || txtNotas.getText().isBlank() ? null : txtNotas.getText().trim());
            r.setActiva(chkActiva.isSelected());
            r.getIngredientes().clear();
            r.getIngredientes().addAll(items);
            recetaDAO.guardar(r);
            seleccionada = null;
            Navegador.ir("ListadoRecetas.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionada = null;
        try { Navegador.ir("ListadoRecetas.fxml"); } catch (Exception ignored) { }
    }

    private void mostrarError(String s) {
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }
    private void limpiarMensaje() {
        lblMensaje.setVisible(false); lblMensaje.setManaged(false); lblMensaje.setText("");
    }
}
