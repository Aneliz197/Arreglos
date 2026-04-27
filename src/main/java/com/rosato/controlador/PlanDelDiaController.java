package com.rosato.controlador;

import com.rosato.dao.DetallePedidoDAO;
import com.rosato.dao.IngredienteDAO;
import com.rosato.dao.PedidoDAO;
import com.rosato.dao.RecetaDAO;
import com.rosato.modelo.DetallePedido;
import com.rosato.modelo.Ingrediente;
import com.rosato.modelo.Pedido;
import com.rosato.modelo.Receta;
import com.rosato.modelo.RecetaIngrediente;
import com.rosato.util.Navegador;
import com.rosato.util.PlanProduccionService;
import com.rosato.util.PlanProduccionService.Requerimiento;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PlanDelDiaController {

    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private DatePicker dpFecha;

    @FXML private TableView<Pedido> tblPedidos;
    @FXML private TableColumn<Pedido, Integer> colPedId;
    @FXML private TableColumn<Pedido, String>  colHora;
    @FXML private TableColumn<Pedido, String>  colCliente;
    @FXML private TableColumn<Pedido, String>  colProducto;
    @FXML private TableColumn<Pedido, BigDecimal> colLibras;
    @FXML private TableColumn<Pedido, String>  colEstado;

    @FXML private TableView<Requerimiento> tblRequerimientos;
    @FXML private TableColumn<Requerimiento, String>     colIng;
    @FXML private TableColumn<Requerimiento, BigDecimal> colReq;
    @FXML private TableColumn<Requerimiento, BigDecimal> colDisp;
    @FXML private TableColumn<Requerimiento, String>     colUniReq;
    @FXML private TableColumn<Requerimiento, String>     colEstadoIng;

    @FXML private Label lblAlerta;

    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final DetallePedidoDAO detalleDAO = new DetallePedidoDAO();
    private final RecetaDAO recetaDAO = new RecetaDAO();
    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();

    // Cache por pedido para no volver a traer detalles al marcar listo
    private final Map<Integer, List<DetallePedido>> detallesPorPedido = new HashMap<>();
    private Map<String, Receta> recetasPorTipo = new HashMap<>();

    @FXML
    private void initialize() {
        dpFecha.setValue(LocalDate.now());

        colPedId   .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getIdPedido()));
        colHora    .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getFechaEntrega() == null ? "" : d.getValue().getFechaEntrega().format(HORA)));
        colCliente .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombreCliente()));
        colProducto.setCellValueFactory(d -> new SimpleStringProperty(resumenProducto(d.getValue())));
        colLibras  .setCellValueFactory(d -> new SimpleObjectProperty<>(totalLibras(d.getValue())));
        colEstado  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado()));

        colIng      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colReq      .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getRequerido()));
        colDisp     .setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDisponible()));
        colUniReq   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUnidad()));
        colEstadoIng.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().estado()));

        onCargar();
    }

    @FXML
    private void onCargar() {
        detallesPorPedido.clear();
        limpiarAlerta();
        try {
            LocalDate f = dpFecha.getValue() == null ? LocalDate.now() : dpFecha.getValue();

            List<Pedido> pedidos = pedidoDAO.listarPorFechaEntrega(f, "Confirmado", "En producción");
            for (Pedido p : pedidos) {
                detallesPorPedido.put(p.getIdPedido(), detalleDAO.porPedido(p.getIdPedido()));
            }
            tblPedidos.setItems(FXCollections.observableArrayList(pedidos));

            recetasPorTipo = recetaDAO.listar().stream()
                    .collect(Collectors.toMap(Receta::getTipoProducto, this::cargarIngredientes,
                            (a, b) -> a, HashMap::new));

            Map<Integer, Ingrediente> ingPorId = new HashMap<>();
            for (Ingrediente i : ingredienteDAO.listar("")) ingPorId.put(i.getIdIngrediente(), i);

            List<DetallePedido> todos = detallesPorPedido.values().stream()
                    .flatMap(List::stream).collect(Collectors.toList());
            List<Requerimiento> req = PlanProduccionService.calcularRequerimientos(
                    todos, recetasPorTipo, ingPorId);
            tblRequerimientos.setItems(FXCollections.observableArrayList(req));

            long sinReceta = pedidos.stream()
                    .flatMap(p -> detallesPorPedido.getOrDefault(p.getIdPedido(), List.of()).stream())
                    .map(DetallePedido::getTipoProducto)
                    .distinct()
                    .filter(t -> !recetasPorTipo.containsKey(t))
                    .count();
            long faltantes = req.stream().filter(Requerimiento::isFaltante).count();
            StringBuilder msg = new StringBuilder();
            if (sinReceta > 0) msg.append("⚠ Hay tipos de producto sin receta registrada. ");
            if (faltantes > 0) msg.append("⚠ ").append(faltantes).append(" ingredientes con faltante de stock.");
            if (msg.length() > 0) mostrarAlerta(msg.toString());
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "No se pudo cargar el plan: " + ex.getMessage()).showAndWait();
        }
    }

    private Receta cargarIngredientes(Receta r) {
        try {
            r.getIngredientes().clear();
            r.getIngredientes().addAll(recetaDAO.ingredientesDe(r.getIdReceta()));
        } catch (Exception ignored) { }
        return r;
    }

    @FXML
    private void onIniciar() {
        Pedido sel = tblPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Selecciona un pedido."); return; }
        if (!"Confirmado".equals(sel.getEstado())) {
            info("Solo se puede iniciar producción desde estado 'Confirmado'."); return;
        }
        try {
            pedidoDAO.actualizarEstado(sel.getIdPedido(), "En producción");
            onCargar();
        } catch (Exception ex) {
            error("No se pudo iniciar producción: " + ex.getMessage());
        }
    }

    @FXML
    private void onMarcarListo() {
        Pedido sel = tblPedidos.getSelectionModel().getSelectedItem();
        if (sel == null) { info("Selecciona un pedido."); return; }
        if (!"En producción".equals(sel.getEstado()) && !"Confirmado".equals(sel.getEstado())) {
            info("Solo se puede marcar como listo un pedido en producción o confirmado."); return;
        }
        // Calcular consumo de ingredientes de este pedido usando las recetas
        Map<Integer, BigDecimal> consumos = new HashMap<>();
        List<DetallePedido> dets = detallesPorPedido.getOrDefault(sel.getIdPedido(), List.of());
        for (DetallePedido d : dets) {
            Receta r = recetasPorTipo.get(d.getTipoProducto());
            if (r == null) {
                error("No hay receta para '" + d.getTipoProducto()
                        + "'. Registra la receta antes de marcar listo."); return;
            }
            BigDecimal libras = d.getCantidadLibras() == null ? BigDecimal.ZERO : d.getCantidadLibras();
            for (RecetaIngrediente ri : r.getIngredientes()) {
                consumos.merge(ri.getFkIdIngrediente(),
                        ri.getCantidadPorLibra().multiply(libras), BigDecimal::add);
            }
        }
        try {
            pedidoDAO.marcarListoConDescuento(sel.getIdPedido(), consumos);
            info("Pedido #" + sel.getIdPedido() + " marcado como 'Listo'. Stock descontado.");
            onCargar();
        } catch (Exception ex) {
            error("No se pudo marcar como listo: " + ex.getMessage());
        }
    }

    @FXML
    private void onVolver() {
        try { Navegador.ir("DashboardEmpleado.fxml"); } catch (Exception ignored) { }
    }

    private String resumenProducto(Pedido p) {
        List<DetallePedido> ds = detallesPorPedido.getOrDefault(p.getIdPedido(), List.of());
        if (ds.isEmpty()) return "";
        return ds.stream().map(DetallePedido::getTipoProducto).collect(Collectors.joining(", "));
    }

    private BigDecimal totalLibras(Pedido p) {
        return detallesPorPedido.getOrDefault(p.getIdPedido(), List.of()).stream()
                .map(DetallePedido::getCantidadLibras)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void info(String s) { new Alert(Alert.AlertType.INFORMATION, s).showAndWait(); }
    private void error(String s) { new Alert(Alert.AlertType.ERROR, s).showAndWait(); }

    private void mostrarAlerta(String s) {
        lblAlerta.setText(s);
        lblAlerta.setVisible(true);
        lblAlerta.setManaged(true);
    }
    private void limpiarAlerta() {
        lblAlerta.setVisible(false); lblAlerta.setManaged(false); lblAlerta.setText("");
    }
}
