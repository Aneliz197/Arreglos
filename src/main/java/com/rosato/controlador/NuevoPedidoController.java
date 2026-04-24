package com.rosato.controlador;

import com.rosato.dao.ClienteDAO;
import com.rosato.dao.PedidoDAO;
import com.rosato.modelo.Cliente;
import com.rosato.modelo.DetallePedido;
import com.rosato.modelo.Pedido;
import com.rosato.util.Navegador;
import com.rosato.util.PrecioService;
import com.rosato.util.Sesion;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pantalla 2.2 - Registro de Nuevo Pedido.
 * Validaciones clave:
 *  - 0.5 lb ≤ cantidad ≤ 20 lb
 *  - hora entrega entre 09:00 y 19:00
 *  - no menos de 3 horas de anticipación si la fecha es hoy
 *  - máximo 1 pedido por cliente en la misma hora
 */
public class NuevoPedidoController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Cliente
    @FXML private TextField txtBuscarCliente;
    @FXML private Label lblClienteSeleccionado;
    @FXML private VBox boxResultadosCliente;

    // Producto
    @FXML private ChoiceBox<String> cbTipo;
    @FXML private TextField txtLibras;
    @FXML private TextArea txtDiseno;
    @FXML private DatePicker dpFechaEntrega;
    @FXML private TextField txtHoraEntrega;
    @FXML private TextArea txtObservaciones;
    @FXML private CheckBox chkComplejo;
    @FXML private CheckBox chkFresas;
    @FXML private HBox boxColaborador;
    @FXML private TextField txtColaborador;

    // Resumen
    @FXML private Label lblPrecioBase;
    @FXML private Label lblCostoDiseno;
    @FXML private Label lblSubtotal;
    @FXML private Label lblAdelanto;
    @FXML private Label lblSaldo;
    @FXML private Label lblTiempo;
    @FXML private Label lblAdvertencia;
    @FXML private Label lblMensaje;

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();

    private Cliente clienteSeleccionado;

    @FXML
    private void initialize() {
        cbTipo.setItems(FXCollections.observableArrayList(PrecioService.tiposProducto()));
        cbTipo.getSelectionModel().selectFirst();
        dpFechaEntrega.setValue(LocalDate.now().plusDays(1));
        txtHoraEntrega.setText("10:00");

        chkComplejo.selectedProperty().addListener((o, a, b) -> {
            boxColaborador.setVisible(b);
            boxColaborador.setManaged(b);
            recalcular();
        });

        cbTipo.valueProperty().addListener((o, a, b) -> recalcular());
        txtLibras.textProperty().addListener((o, a, b) -> recalcular());
        chkFresas.selectedProperty().addListener((o, a, b) -> recalcular());

        // Si el usuario es cliente, autoseleccionarlo
        if (Sesion.esCliente()) {
            try {
                clienteDAO.porId(Sesion.getIdUsuario()).ifPresent(this::seleccionarCliente);
            } catch (Exception ignored) { }
        }

        recalcular();
    }

    // ---------------- Cliente ----------------

    @FXML
    private void onBuscarCliente() {
        boxResultadosCliente.getChildren().clear();
        try {
            List<Cliente> lista = clienteDAO.listar(txtBuscarCliente.getText());
            if (lista.isEmpty()) {
                boxResultadosCliente.getChildren().add(new Label("Sin resultados."));
                return;
            }
            for (Cliente c : lista.subList(0, Math.min(8, lista.size()))) {
                HBox fila = new HBox(10);
                fila.setAlignment(Pos.CENTER_LEFT);
                Label lbl = new Label("#" + c.getIdCliente() + " · " + c.getNombreCompleto()
                        + " · " + c.getTelefono());
                HBox.setHgrow(lbl, Priority.ALWAYS);
                Button btn = new Button("Seleccionar");
                btn.getStyleClass().add("btn-secundario");
                btn.setOnAction(e -> seleccionarCliente(c));
                fila.getChildren().addAll(lbl, btn);
                boxResultadosCliente.getChildren().add(fila);
            }
        } catch (Exception ex) {
            mostrarError("Error buscando clientes: " + ex.getMessage());
        }
    }

    @FXML
    private void onNuevoClienteRapido() {
        Dialog<Cliente> dlg = new Dialog<>();
        dlg.setTitle("Cliente nuevo");
        dlg.setHeaderText("Registro rápido (nombre y teléfono)");
        ButtonType btGuardar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(btGuardar, ButtonType.CANCEL);

        VBox box = new VBox(8);
        TextField tNombre = new TextField();   tNombre.setPromptText("Nombre");
        TextField tApellido = new TextField(); tApellido.setPromptText("Apellido");
        TextField tTel = new TextField();      tTel.setPromptText("Teléfono (10+ dígitos)");
        box.getChildren().addAll(new Label("Nombre"), tNombre,
                new Label("Apellido"), tApellido, new Label("Teléfono"), tTel);
        dlg.getDialogPane().setContent(box);

        dlg.setResultConverter(b -> {
            if (b == btGuardar) {
                if (tNombre.getText().isBlank() || tApellido.getText().isBlank()
                        || tTel.getText().replaceAll("[^0-9]", "").length() < 10) {
                    return null;
                }
                Cliente c = new Cliente();
                c.setNombre(tNombre.getText().trim());
                c.setApellido(tApellido.getText().trim());
                c.setTelefono(tTel.getText().trim());
                c.setDireccion("-");
                c.setEmail("cliente" + System.currentTimeMillis() + "@pendiente.local");
                c.setUsuario("cli_" + System.currentTimeMillis());
                c.setContrasena("$2a$10$pendiente");   // sin login directo
                c.setAceptaTerminos(false);
                return c;
            }
            return null;
        });

        dlg.showAndWait().ifPresent(c -> {
            try {
                clienteDAO.insertar(c);
                seleccionarCliente(c);
            } catch (Exception ex) {
                mostrarError("No se pudo crear el cliente: " + ex.getMessage());
            }
        });
    }

    private void seleccionarCliente(Cliente c) {
        clienteSeleccionado = c;
        lblClienteSeleccionado.setText("Cliente: " + c.getNombreCompleto()
                + " · Tel " + c.getTelefono()
                + (c.getDireccion() == null ? "" : " · " + c.getDireccion()));
        boxResultadosCliente.getChildren().clear();
    }

    // ---------------- Cálculo ----------------

    @FXML
    private void onRecalcular() { recalcular(); }

    private void recalcular() {
        limpiarMensaje();
        BigDecimal libras = parseLibras();
        String tipo = cbTipo.getValue();
        BigDecimal pb  = PrecioService.precioBase(tipo, libras);
        BigDecimal cd  = PrecioService.costoDiseno(pb, chkComplejo.isSelected());
        BigDecimal sub = PrecioService.subtotal(pb, cd);
        BigDecimal adl = PrecioService.adelanto50(sub);
        BigDecimal sal = PrecioService.saldoPendiente(sub, adl);
        BigDecimal hrs = PrecioService.tiempoEstimadoHoras(libras);

        lblPrecioBase .setText("Precio base:      RD$ " + pb);
        lblCostoDiseno.setText("Costo diseño:     RD$ " + cd);
        lblSubtotal   .setText("Subtotal:         RD$ " + sub);
        lblAdelanto   .setText("Adelanto 50%:     RD$ " + adl);
        lblSaldo      .setText("Saldo pendiente:  RD$ " + sal);
        lblTiempo     .setText("Tiempo estimado:  " + hrs + " h");

        boolean adv = chkFresas.isSelected();
        lblAdvertencia.setVisible(adv);
        lblAdvertencia.setManaged(adv);
        if (adv) lblAdvertencia.setText("Producto delicado: fresas no garantizadas después de 24h.");
    }

    private BigDecimal parseLibras() {
        try {
            String s = txtLibras.getText();
            if (s == null || s.isBlank()) return BigDecimal.ZERO;
            return new BigDecimal(s.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    // ---------------- Guardar ----------------

    @FXML
    private void onGuardarBorrador() {
        guardar("Borrador");
    }

    @FXML
    private void onSolicitarConfirmacion() {
        Integer idPedido = guardar("Pendiente verificación");
        if (idPedido != null) {
            try {
                Navegador.ir("ConfirmacionPedido.fxml");
                // Pasar el ID al siguiente controller vía propiedad estática simple.
                com.rosato.controlador.ConfirmacionPedidoController.setIdPedido(idPedido);
            } catch (Exception ex) {
                mostrarError("Pedido guardado (id " + idPedido + ") pero no se pudo abrir la confirmación: "
                        + ex.getMessage());
            }
        }
    }

    /** Valida + persiste. Devuelve el id del pedido o null si falló la validación. */
    private Integer guardar(String estadoInicial) {
        if (clienteSeleccionado == null) {
            mostrarError("Selecciona un cliente antes de guardar."); return null;
        }
        BigDecimal libras = parseLibras();
        if (!PrecioService.librasValidas(libras)) {
            mostrarError("La cantidad debe estar entre 0.5 y 20 libras."); return null;
        }
        if (txtDiseno.getText() == null || txtDiseno.getText().isBlank()) {
            mostrarError("El diseño es obligatorio."); return null;
        }

        LocalDate fecha = dpFechaEntrega.getValue();
        if (fecha == null) {
            mostrarError("Selecciona la fecha de entrega."); return null;
        }
        LocalTime hora = parseHora(txtHoraEntrega.getText());
        if (hora == null) {
            mostrarError("Hora de entrega inválida. Formato HH:MM."); return null;
        }
        if (hora.isBefore(LocalTime.of(9, 0)) || hora.isAfter(LocalTime.of(19, 0))) {
            mostrarError("La hora de entrega debe estar entre 09:00 y 19:00."); return null;
        }
        LocalDateTime fechaEntrega = LocalDateTime.of(fecha, hora);
        if (fechaEntrega.isBefore(LocalDateTime.now().plusHours(3))) {
            mostrarError("La entrega requiere al menos 3 horas de anticipación."); return null;
        }
        if (chkComplejo.isSelected()
                && (txtColaborador.getText() == null || txtColaborador.getText().isBlank())) {
            mostrarError("Los diseños complejos requieren un colaborador externo."); return null;
        }

        try {
            if (pedidoDAO.contarPedidosEnMismaHora(clienteSeleccionado.getIdCliente(), fechaEntrega) > 0) {
                mostrarError("El cliente ya tiene un pedido en esa misma hora de entrega.");
                return null;
            }

            BigDecimal pb  = PrecioService.precioBase(cbTipo.getValue(), libras);
            BigDecimal cd  = PrecioService.costoDiseno(pb, chkComplejo.isSelected());
            BigDecimal sub = PrecioService.subtotal(pb, cd);
            BigDecimal adl = PrecioService.adelanto50(sub);
            BigDecimal sal = PrecioService.saldoPendiente(sub, adl);

            Pedido p = new Pedido();
            p.setFkIdCliente(clienteSeleccionado.getIdCliente());
            if (Sesion.esEmpleado()) p.setFkIdEmpleado(Sesion.getIdUsuario());
            p.setFechaEntrega(fechaEntrega);
            p.setEstado(estadoInicial);
            p.setTipoEntrega("Local");
            p.setSubtotal(sub);
            p.setAdelanto(BigDecimal.ZERO);       // adelanto real se cobra en pantalla 2.3
            p.setSaldoPendiente(sub);
            p.setObservaciones(txtObservaciones.getText());

            DetallePedido d = new DetallePedido();
            d.setTipoProducto(cbTipo.getValue());
            d.setCantidadLibras(libras);
            d.setDiseno(txtDiseno.getText().trim());
            d.setDisenoComplejo(chkComplejo.isSelected());
            d.setColaboradorExterno(chkComplejo.isSelected() ? txtColaborador.getText().trim() : null);
            d.setTiempoEstimadoHoras(PrecioService.tiempoEstimadoHoras(libras));
            d.setPrecioBase(pb);
            d.setCostoDiseno(cd);
            d.setContieneFresas(chkFresas.isSelected());

            try (var conn = pedidoDAO.abrirConexion()) {
                conn.setAutoCommit(false);
                try {
                    int idPedido = pedidoDAO.insertar(conn, p);
                    d.setFkIdPedido(idPedido);
                    new com.rosato.dao.DetallePedidoDAO().insertar(conn, d);
                    conn.commit();

                    mostrarOk("Pedido #" + idPedido + " guardado en estado '" + estadoInicial + "'. "
                            + "Entrega " + fechaEntrega.format(FMT) + ", adelanto a cobrar RD$ " + adl + ".");
                    return idPedido;
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            }
        } catch (Exception ex) {
            mostrarError("Error guardando el pedido: " + ex.getMessage());
            return null;
        }
    }

    private LocalTime parseHora(String s) {
        if (s == null) return null;
        try {
            return LocalTime.parse(s.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    @FXML
    private void onVolver() {
        try {
            String destino = Sesion.esEmpleado() ? "DashboardEmpleado.fxml" : "DashboardCliente.fxml";
            Navegador.ir(destino);
        } catch (Exception ignored) { }
    }

    private void mostrarError(String s) {
        lblMensaje.getStyleClass().removeAll("ok");
        if (!lblMensaje.getStyleClass().contains("error")) lblMensaje.getStyleClass().add("error");
        lblMensaje.setText(s);
        lblMensaje.setVisible(true);
        lblMensaje.setManaged(true);
    }

    private void mostrarOk(String s) {
        lblMensaje.getStyleClass().removeAll("error");
        if (!lblMensaje.getStyleClass().contains("ok")) lblMensaje.getStyleClass().add("ok");
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
