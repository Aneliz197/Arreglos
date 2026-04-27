package com.rosato.controlador;

import com.rosato.dao.EntregaDAO;
import com.rosato.modelo.Entrega;
import com.rosato.util.Navegador;
import com.rosato.util.Sesion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public class EntregarYCobrarController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final String[] METODOS = {"Efectivo", "Tarjeta", "Transferencia"};

    private static Entrega seleccionada;
    public static void setEntrega(Entrega e) { seleccionada = e; }

    @FXML private Label lblPedido;
    @FXML private Label lblCliente;
    @FXML private Label lblTipo;
    @FXML private Label lblDireccion;
    @FXML private Label lblFechaProg;
    @FXML private Label lblSaldo;

    @FXML private TextField txtMonto;
    @FXML private ChoiceBox<String> cbMetodo;
    @FXML private TextField txtReferencia;

    @FXML private Label lblMensaje;

    private final EntregaDAO entregaDAO = new EntregaDAO();

    @FXML
    private void initialize() {
        cbMetodo.setItems(FXCollections.observableArrayList(METODOS));
        cbMetodo.setValue("Efectivo");

        if (seleccionada == null) {
            mostrarError("No hay entrega seleccionada.");
            return;
        }
        lblPedido.setText("Entrega #" + seleccionada.getIdEntrega()
                + " · Pedido #" + seleccionada.getFkIdPedido());
        lblCliente.setText("Cliente: " + (seleccionada.getNombreCliente() == null ? "-" : seleccionada.getNombreCliente()));
        lblTipo.setText("Tipo: " + (seleccionada.getTipoEntrega() == null ? "-" : seleccionada.getTipoEntrega()));
        lblDireccion.setText("Dirección: " + (seleccionada.getDireccion() == null ? "(local)" : seleccionada.getDireccion()));
        lblFechaProg.setText("Programada: " + (seleccionada.getFechaProgramada() == null ? "-" : seleccionada.getFechaProgramada().format(FMT)));
        BigDecimal saldo = seleccionada.getSaldoPendiente() == null ? BigDecimal.ZERO : seleccionada.getSaldoPendiente();
        lblSaldo.setText("Saldo pendiente: RD$ " + saldo);
        txtMonto.setText(saldo.toPlainString());
    }

    @FXML
    private void onCobrar() {
        limpiarMensaje();
        if (seleccionada == null) { mostrarError("Sin entrega."); return; }
        BigDecimal monto;
        try {
            if (txtMonto.getText() == null || txtMonto.getText().isBlank()) {
                mostrarError("Indica el monto."); return;
            }
            monto = new BigDecimal(txtMonto.getText().trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            mostrarError("Monto inválido."); return;
        }
        if (monto.signum() <= 0) { mostrarError("El monto debe ser > 0."); return; }

        String metodo = cbMetodo.getValue();
        if (metodo == null) { mostrarError("Selecciona un método de pago."); return; }

        String ref = txtReferencia.getText() == null ? null : txtReferencia.getText().trim();
        if (ref != null && ref.isEmpty()) ref = null;
        if (!"Efectivo".equals(metodo) && (ref == null)) {
            mostrarError("Referencia obligatoria para " + metodo + "."); return;
        }

        try {
            Integer empleado = Sesion.esEmpleado() ? Sesion.getIdUsuario() : null;
            entregaDAO.entregarYCobrar(seleccionada.getIdEntrega(), seleccionada.getFkIdPedido(),
                    monto, metodo, ref, empleado);
            seleccionada = null;
            Navegador.ir("AgendaEntregas.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo registrar el cobro: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        seleccionada = null;
        try { Navegador.ir("AgendaEntregas.fxml"); } catch (Exception ignored) { }
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
