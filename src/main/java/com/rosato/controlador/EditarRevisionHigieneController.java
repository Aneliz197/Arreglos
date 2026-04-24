package com.rosato.controlador;

import com.rosato.dao.RevisionHigieneDAO;
import com.rosato.modelo.RevisionHigiene;
import com.rosato.modelo.RevisionHigieneItem;
import com.rosato.util.HigieneCalc;
import com.rosato.util.Navegador;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EditarRevisionHigieneController {

    private static final String[] PREDEFINIDOS = {
            "Pisos, paredes y techos limpios",
            "Superficies y utensilios sanitizados",
            "Basureros con tapa y vaciados",
            "Control de plagas vigente",
            "Contenedores de ingredientes tapados",
            "Temperatura de refrigeración correcta",
            "Lavamanos equipados (jabón, papel)",
            "Sanitarios limpios y abastecidos"
    };

    private static RevisionHigiene seleccionada;
    public static void setSeleccionada(RevisionHigiene r) { seleccionada = r; }

    @FXML private Label lblTitulo;
    @FXML private DatePicker dpFecha;
    @FXML private ChoiceBox<String> cbArea;
    @FXML private TextField txtResponsable;
    @FXML private TextArea  txtObservaciones;

    @FXML private TableView<RevisionHigieneItem> tblItems;
    @FXML private TableColumn<RevisionHigieneItem, String>  colDesc;
    @FXML private TableColumn<RevisionHigieneItem, Boolean> colCumple;
    @FXML private TableColumn<RevisionHigieneItem, String>  colObs;

    @FXML private Label lblResumen;
    @FXML private Label lblMensaje;

    private final RevisionHigieneDAO dao = new RevisionHigieneDAO();

    @FXML
    private void initialize() {
        List<String> areas = java.util.Arrays.stream(ListadoRevisionesHigieneController.AREAS)
                .filter(a -> !"Todas".equalsIgnoreCase(a)).toList();
        cbArea.setItems(FXCollections.observableArrayList(areas));

        configurarTabla();

        if (seleccionada == null) {
            lblTitulo.setText("Nueva revisión");
            dpFecha.setValue(LocalDate.now());
            cbArea.setValue(areas.get(0));
            tblItems.setItems(FXCollections.observableArrayList());
        } else {
            lblTitulo.setText("Revisión #" + seleccionada.getIdRevision());
            dpFecha.setValue(seleccionada.getFecha());
            cbArea.setValue(seleccionada.getArea());
            txtResponsable.setText(seleccionada.getResponsable());
            txtObservaciones.setText(seleccionada.getObservaciones());
            tblItems.setItems(FXCollections.observableArrayList(seleccionada.getItems()));
        }
        actualizarResumen();
    }

    private void configurarTabla() {
        colDesc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDescripcion()));
        colDesc.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        colDesc.setOnEditCommit(ev -> ev.getRowValue().setDescripcion(ev.getNewValue()));
        colDesc.setEditable(true);

        colCumple.setCellValueFactory(d -> {
            SimpleBooleanProperty p = new SimpleBooleanProperty(d.getValue().isCumple());
            p.addListener((obs, oldV, newV) -> { d.getValue().setCumple(newV); actualizarResumen(); });
            return p;
        });
        colCumple.setCellFactory(CheckBoxTableCell.forTableColumn(colCumple));
        colCumple.setEditable(true);

        colObs.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getObservacion()));
        colObs.setCellFactory(TextFieldTableCell.forTableColumn(new DefaultStringConverter()));
        colObs.setOnEditCommit(ev -> ev.getRowValue().setObservacion(ev.getNewValue()));
        colObs.setEditable(true);
    }

    private void actualizarResumen() {
        int total = tblItems.getItems().size();
        int ok = 0;
        for (RevisionHigieneItem it : tblItems.getItems()) if (it.isCumple()) ok++;
        BigDecimal pct = HigieneCalc.porcentaje(ok, total);
        String estado = HigieneCalc.estadoLabel(HigieneCalc.evaluar(pct));
        lblResumen.setText("Cumplimiento: " + ok + "/" + total + " · " + pct + "% · " + estado);
    }

    @FXML
    private void onAgregarItem() {
        tblItems.getItems().add(new RevisionHigieneItem(""));
        actualizarResumen();
    }

    @FXML
    private void onQuitarItem() {
        RevisionHigieneItem sel = tblItems.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        tblItems.getItems().remove(sel);
        actualizarResumen();
    }

    @FXML
    private void onPredefinidos() {
        if (!tblItems.getItems().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Esto reemplazará el checklist actual con los items predefinidos. ¿Continuar?",
                    ButtonType.OK, ButtonType.CANCEL);
            if (a.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        }
        tblItems.getItems().clear();
        for (String d : PREDEFINIDOS) tblItems.getItems().add(new RevisionHigieneItem(d));
        actualizarResumen();
    }

    @FXML
    private void onGuardar() {
        limpiarMensaje();
        if (dpFecha.getValue() == null || cbArea.getValue() == null) {
            mostrarError("Fecha y área son obligatorios."); return;
        }
        if (tblItems.getItems().isEmpty()) {
            mostrarError("Agrega al menos un item al checklist."); return;
        }
        for (RevisionHigieneItem it : tblItems.getItems()) {
            if (it.getDescripcion() == null || it.getDescripcion().isBlank()) {
                mostrarError("Hay items sin descripción."); return;
            }
        }
        RevisionHigiene r = seleccionada == null ? new RevisionHigiene() : seleccionada;
        r.setFecha(dpFecha.getValue());
        r.setArea(cbArea.getValue());
        r.setResponsable(vacioANull(txtResponsable.getText()));
        r.setObservaciones(vacioANull(txtObservaciones.getText()));
        r.setItems(new java.util.ArrayList<>(tblItems.getItems()));
        try {
            dao.guardar(r);
            seleccionada = null;
            Navegador.ir("ListadoRevisionesHigiene.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo guardar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionada = null;
        try { Navegador.ir("ListadoRevisionesHigiene.fxml"); } catch (Exception ignored) { }
    }

    private static String vacioANull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
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
