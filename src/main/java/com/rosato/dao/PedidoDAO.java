package com.rosato.dao;

import com.rosato.modelo.Pedido;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO de Pedido (Módulo 2). */
public class PedidoDAO {

    public Connection abrirConexion() throws SQLException {
        return ConexionBD.get();
    }

    /** Inserta un pedido usando la conexión provista (para transacción). */
    public int insertar(Connection c, Pedido p) throws SQLException {
        String sql = """
            INSERT INTO Pedido
              (fk_id_cliente, fk_id_empleado, fecha_entrega, estado, tipo_entrega,
               subtotal, adelanto, saldo_pendiente, observaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getFkIdCliente());
            if (p.getFkIdEmpleado() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, p.getFkIdEmpleado());
            ps.setTimestamp(3, Timestamp.valueOf(p.getFechaEntrega()));
            ps.setString(4, p.getEstado());
            ps.setString(5, p.getTipoEntrega());
            ps.setBigDecimal(6, p.getSubtotal());
            ps.setBigDecimal(7, p.getAdelanto());
            ps.setBigDecimal(8, p.getSaldoPendiente());
            ps.setString(9, p.getObservaciones());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) {
                    p.setIdPedido(k.getInt(1));
                    return p.getIdPedido();
                }
            }
        }
        throw new SQLException("No se pudo insertar el pedido.");
    }

    public void actualizarEstado(int idPedido, String nuevoEstado) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE Pedido SET estado = ? WHERE id_pedido = ?")) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
        }
    }

    /**
     * Marca el pedido como 'Listo' descontando el stock de cada ingrediente
     * según sus recetas.  Todo en una sola transacción; si algún insumo no
     * alcanza se lanza SQLException y se hace rollback.
     *
     * @param consumos mapa id_ingrediente → cantidad a descontar
     */
    public void marcarListoConDescuento(int idPedido,
                                        java.util.Map<Integer, java.math.BigDecimal> consumos)
            throws SQLException {
        try (Connection c = ConexionBD.get()) {
            c.setAutoCommit(false);
            try {
                // Validar stock suficiente antes de descontar
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT nombre, stock_actual FROM Ingrediente WHERE id_ingrediente = ?")) {
                    for (var e : consumos.entrySet()) {
                        ps.setInt(1, e.getKey());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()) throw new SQLException("Ingrediente no encontrado: " + e.getKey());
                            java.math.BigDecimal stock = rs.getBigDecimal("stock_actual");
                            if (stock.compareTo(e.getValue()) < 0) {
                                throw new SQLException("Stock insuficiente de '"
                                        + rs.getString("nombre") + "' (requiere "
                                        + e.getValue() + ", hay " + stock + ")");
                            }
                        }
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Ingrediente SET stock_actual = stock_actual - ? WHERE id_ingrediente = ?")) {
                    for (var e : consumos.entrySet()) {
                        ps.setBigDecimal(1, e.getValue());
                        ps.setInt(2, e.getKey());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Pedido SET estado = 'Listo' WHERE id_pedido = ?")) {
                    ps.setInt(1, idPedido);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException se) throw se;
                throw new SQLException(ex);
            }
        }
    }

    /** Cuenta pedidos del cliente que solapan dentro de la misma hora de entrega. */
    public int contarPedidosEnMismaHora(int idCliente, LocalDateTime fechaHora) throws SQLException {
        String sql = """
            SELECT COUNT(*) FROM Pedido
            WHERE fk_id_cliente = ?
              AND fecha_entrega >= ?
              AND fecha_entrega <  ?
              AND estado <> 'Cancelado'
            """;
        LocalDateTime desde = fechaHora.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime hasta = desde.plusHours(1);
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setTimestamp(2, Timestamp.valueOf(desde));
            ps.setTimestamp(3, Timestamp.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }


    public List<Pedido> listarTodos() throws SQLException {
        String sql = """
            SELECT p.*, CONCAT(c.nombre, ' ', c.apellido) AS nombre_cli
            FROM Pedido p
            JOIN Cliente c ON c.id_cliente = p.fk_id_cliente
            ORDER BY p.fecha_pedido DESC
            """;
        List<Pedido> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapear(rs));
        }
        return out;
    }

    /**
     * Lista pedidos con fecha de entrega en el día indicado y en los
     * estados dados (ej. 'Confirmado', 'En producción').
     */
    public List<Pedido> listarPorFechaEntrega(java.time.LocalDate fecha, String... estados) throws SQLException {
        String inClause = estados.length == 0 ? "" :
                " AND p.estado IN (" + "?,".repeat(estados.length).replaceAll(",$", "") + ")";
        String sql = """
            SELECT p.*, CONCAT(c.nombre, ' ', c.apellido) AS nombre_cli
            FROM Pedido p
            JOIN Cliente c ON c.id_cliente = p.fk_id_cliente
            WHERE p.fecha_entrega >= ? AND p.fecha_entrega < ?
            """ + inClause + " ORDER BY p.fecha_entrega";
        java.time.LocalDateTime desde = fecha.atStartOfDay();
        java.time.LocalDateTime hasta = fecha.plusDays(1).atStartOfDay();
        List<Pedido> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            for (int i = 0; i < estados.length; i++) ps.setString(3 + i, estados[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public List<Pedido> listarPorCliente(int idCliente) throws SQLException {
        String sql = """
            SELECT p.*, CONCAT(c.nombre, ' ', c.apellido) AS nombre_cli
            FROM Pedido p
            JOIN Cliente c ON c.id_cliente = p.fk_id_cliente
            WHERE p.fk_id_cliente = ?
            ORDER BY p.fecha_pedido DESC
            """;
        List<Pedido> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setIdPedido(rs.getInt("id_pedido"));
        p.setFkIdCliente(rs.getInt("fk_id_cliente"));
        int emp = rs.getInt("fk_id_empleado");
        if (!rs.wasNull()) p.setFkIdEmpleado(emp);
        Timestamp fp = rs.getTimestamp("fecha_pedido");
        if (fp != null) p.setFechaPedido(fp.toLocalDateTime());
        Timestamp fe = rs.getTimestamp("fecha_entrega");
        if (fe != null) p.setFechaEntrega(fe.toLocalDateTime());
        p.setEstado(rs.getString("estado"));
        p.setTipoEntrega(rs.getString("tipo_entrega"));
        p.setSubtotal(rs.getBigDecimal("subtotal"));
        p.setAdelanto(rs.getBigDecimal("adelanto"));
        p.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente"));
        p.setObservaciones(rs.getString("observaciones"));
        try { p.setNombreCliente(rs.getString("nombre_cli")); } catch (SQLException ignore) { }
        return p;
    }
}
