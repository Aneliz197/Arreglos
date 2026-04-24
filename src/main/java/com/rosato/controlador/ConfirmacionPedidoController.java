package com.rosato.controlador;

import com.rosato.dao.ClienteDAO;
import com.rosato.dao.DetallePedidoDAO;
import com.rosato.dao.EntregaDAO;
import com.rosato.dao.PagoDAO;
import com.rosato.dao.PedidoDAO;
import com.rosato.modelo.Cliente;
import com.rosato.modelo.DetallePedido;
import com.rosato.modelo.Entrega;
import com.rosato.modelo.Pedido;
import com.rosato.util.Navegador;
import com.rosato.util.Sesion;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Pantalla 2.3 - Confirmación y verificación del pedido.
 * Muestra el resumen del último pedido creado y, al confirmar,
 * cambia el estado a "Confirmado" y registra el adelanto 50%.
 */
public class ConfirmacionPedidoController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Id del pedido que se está confirmando, pasado desde NuevoPedidoController. */
    private static Integer idPedidoActual;

    public static void setIdPedido(Integer id) { idPedidoActual = id; }

    @FXML private Label lblEncabezado;
    @FXML private Label lblCliente;
    @FXML private Label lblProducto;
    @FXML private Label lblFechas;
    @FXML private Label lblSubtotal;
    @FXML private Label lblAdelanto;
    @FXML private Label lblSaldo;
    @FXML private Label lblEstado;
    @FXML private Label lblTiempo;
    @FXML private Label lblDecorador;
    @FXML private Label lblFresas;
    @FXML private Label lblMensaje;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final EntregaDAO entregaDAO = new EntregaDAO();

    private Pedido pedido;
    private DetallePedido detalle;
    private Cliente cliente;

    @FXML
    private void initialize() {
        if (idPedidoActual == null) {
            mostrarError("No hay pedido seleccionado para confirmar.");
            return;
        }
        cargar();
    }

    private void cargar() {
        try {
            List<Pedido> pedidos = pedidoDAO.listarTodos();
            pedido = pedidos.stream()
                    .filter(p -> p.getIdPedido().equals(idPedidoActual))
                    .findFirst().orElse(null);
            if (pedido == null) {
                mostrarError("No se encontró el pedido #" + idPedidoActual);
                return;
            }
            List<DetallePedido> detalles = detalleDAO.porPedido(pedido.getIdPedido());
            detalle = detalles.isEmpty() ? null : detalles.get(0);
            cliente = clienteDAO.porId(pedido.getFkIdCliente()).orElse(null);

            lblEncabezado.setText("Pedido #" + pedido.getIdPedido());
            lblCliente.setText("Cliente: " + (cliente == null ? "(?)" : cliente.getNombreCompleto()
                    + " · " + cliente.getTelefono()
                    + (cliente.getDireccion() == null ? "" : " · " + cliente.getDireccion())));
            if (detalle != null) {
                lblProducto.setText("Producto: " + detalle.getTipoProducto()
                        + " · " + detalle.getCantidadLibras() + " lb"
                        + (detalle.isDisenoComplejo() ? " · diseño complejo" : "")
                        + " · " + detalle.getDiseno());
            } else {
                lblProducto.setText("Producto: (sin detalle)");
            }
            lblFechas.setText("Pedido: " + (pedido.getFechaPedido() == null ? "-" : pedido.getFechaPedido().format(FMT))
                    + "   |   Entrega: " + pedido.getFechaEntrega().format(FMT));

            BigDecimal subtotal = pedido.getSubtotal();
            BigDecimal adelanto = subtotal.multiply(new BigDecimal("0.5"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            BigDecimal saldo = subtotal.subtract(adelanto);

            lblSubtotal.setText("Subtotal:         RD$ " + subtotal);
            lblAdelanto.setText("Adelanto 50%:     RD$ " + adelanto);
            lblSaldo   .setText("Saldo pendiente:  RD$ " + saldo);
            lblEstado  .setText("Estado actual:    " + pedido.getEstado());

            if (detalle != null && detalle.getTiempoEstimadoHoras() != null) {
                lblTiempo.setText("⏱️ Tiempo estimado: " + detalle.getTiempoEstimadoHoras() + " h");
            }
            if (detalle != null && detalle.isDisenoComplejo()) {
                String col = detalle.getColaboradorExterno();
                lblDecorador.setText("🧑‍🍳 Decorador asignado: " + (col == null ? "(sin asignar)" : col));
            } else {
                lblDecorador.setText("🧑‍🍳 Diseño simple: no requiere colaborador externo");
            }
            if (detalle != null && detalle.isContieneFresas()) {
                lblFresas.setText("🍓 Contiene fresas: no se garantiza frescura después de 24 h.");
                lblFresas.setVisible(true);
                lblFresas.setManaged(true);
            }

        } catch (Exception ex) {
            mostrarError("Error cargando el pedido: " + ex.getMessage());
        }
    }

    @FXML
    private void onConfirmar() {
        if (pedido == null) return;
        try {
            BigDecimal subtotal = pedido.getSubtotal();
            BigDecimal adelanto = subtotal.multiply(new BigDecimal("0.5"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);

            try (var conn = pedidoDAO.abrirConexion()) {
                conn.setAutoCommit(false);
                try {
                    // Registrar adelanto 50% (marcador; el cobro efectivo va en pantalla 5.1).
                    pagoDAO.registrar(conn, pedido.getIdPedido(),
                            "Adelanto 50%", "Efectivo", adelanto, null,
                            Sesion.esEmpleado() ? Sesion.getIdUsuario() : null);
                    try (var ps = conn.prepareStatement(
                            "UPDATE Pedido SET estado = 'Confirmado', adelanto = ?, saldo_pendiente = ? WHERE id_pedido = ?")) {
                        ps.setBigDecimal(1, adelanto);
                        ps.setBigDecimal(2, subtotal.subtract(adelanto));
                        ps.setInt(3, pedido.getIdPedido());
                        ps.executeUpdate();
                    }
                    // Crear la Entrega asociada al pedido confirmado.
                    if (entregaDAO.porPedido(pedido.getIdPedido()).isEmpty()) {
                        Entrega entrega = new Entrega();
                        entrega.setFkIdPedido(pedido.getIdPedido());
                        entrega.setTipoEntrega(pedido.getTipoEntrega() == null
                                ? "Local" : pedido.getTipoEntrega());
                        entrega.setDireccion(cliente == null ? null : cliente.getDireccion());
                        entrega.setFechaProgramada(pedido.getFechaEntrega());
                        entrega.setEstado("Pendiente");
                        entregaDAO.insertar(conn, entrega);
                    }
                    conn.commit();
                } catch (Exception ex) {
                    conn.rollback();
                    throw ex;
                }
            }

            mostrarOk("Pedido #" + pedido.getIdPedido() + " confirmado. Adelanto registrado.");
            idPedidoActual = null;
            Navegador.ir(Sesion.esEmpleado() ? "ListadoPedidos.fxml" : "DashboardCliente.fxml");
        } catch (Exception ex) {
            mostrarError("No se pudo confirmar: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("NuevoPedido.fxml"); } catch (Exception ignored) { }
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
}
