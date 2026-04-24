package com.rosato.dao;

import com.rosato.modelo.Entrega;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntregaDAO {

    private final PagoDAO pagoDAO = new PagoDAO();

    /**
     * Inserta una entrega usando la conexión provista (para ser llamada
     * dentro de la transacción de confirmación del pedido).
     */
    public int insertar(Connection c, Entrega e) throws SQLException {
        String sql = """
            INSERT INTO Entrega
              (fk_id_pedido, tipo_entrega, direccion, fecha_programada, estado, fk_id_repartidor, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getFkIdPedido());
            ps.setString(2, e.getTipoEntrega());
            if (e.getDireccion() == null) ps.setNull(3, Types.NVARCHAR); else ps.setString(3, e.getDireccion());
            ps.setTimestamp(4, Timestamp.valueOf(e.getFechaProgramada()));
            ps.setString(5, e.getEstado() == null ? "Pendiente" : e.getEstado());
            if (e.getFkIdRepartidor() == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, e.getFkIdRepartidor());
            if (e.getNotas() == null) ps.setNull(7, Types.NVARCHAR); else ps.setString(7, e.getNotas());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) { e.setIdEntrega(k.getInt(1)); return e.getIdEntrega(); }
            }
        }
        throw new SQLException("No se pudo insertar la entrega.");
    }

    public Optional<Entrega> porId(int id) throws SQLException {
        String sql = """
            SELECT e.*, CONCAT(c.nombre, ' ', c.apellido) AS nombre_cli,
                   CONCAT(emp.nombre, ' ', emp.apellido) AS nombre_rep,
                   p.saldo_pendiente, p.estado AS estado_pedido
            FROM Entrega e
            JOIN Pedido p   ON p.id_pedido = e.fk_id_pedido
            JOIN Cliente c  ON c.id_cliente = p.fk_id_cliente
            LEFT JOIN Empleado emp ON emp.id_empleado = e.fk_id_repartidor
            WHERE e.id_entrega = ?
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Entrega> porPedido(int idPedido) throws SQLException {
        String sql = """
            SELECT e.*, CONCAT(c.nombre, ' ', c.apellido) AS nombre_cli,
                   CONCAT(emp.nombre, ' ', emp.apellido) AS nombre_rep,
                   p.saldo_pendiente, p.estado AS estado_pedido
            FROM Entrega e
            JOIN Pedido p   ON p.id_pedido = e.fk_id_pedido
            JOIN Cliente c  ON c.id_cliente = p.fk_id_cliente
            LEFT JOIN Empleado emp ON emp.id_empleado = e.fk_id_repartidor
            WHERE e.fk_id_pedido = ?
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Entrega> listarPorFecha(LocalDate fecha, String estadoFiltro) throws SQLException {
        String base = """
            SELECT e.*, CONCAT(c.nombre, ' ', c.apellido) AS nombre_cli,
                   CONCAT(emp.nombre, ' ', emp.apellido) AS nombre_rep,
                   p.saldo_pendiente, p.estado AS estado_pedido
            FROM Entrega e
            JOIN Pedido p   ON p.id_pedido = e.fk_id_pedido
            JOIN Cliente c  ON c.id_cliente = p.fk_id_cliente
            LEFT JOIN Empleado emp ON emp.id_empleado = e.fk_id_repartidor
            WHERE e.fecha_programada >= ? AND e.fecha_programada < ?
            """;
        String sql = base + (estadoFiltro == null || estadoFiltro.isBlank() || "Todos".equalsIgnoreCase(estadoFiltro)
                ? "" : " AND e.estado = ?") + " ORDER BY e.fecha_programada";
        LocalDateTime desde = fecha.atStartOfDay();
        LocalDateTime hasta = fecha.plusDays(1).atStartOfDay();
        List<Entrega> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(desde));
            ps.setTimestamp(2, Timestamp.valueOf(hasta));
            if (estadoFiltro != null && !estadoFiltro.isBlank() && !"Todos".equalsIgnoreCase(estadoFiltro)) {
                ps.setString(3, estadoFiltro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public void cambiarEstado(int idEntrega, String nuevoEstado) throws SQLException {
        String sql = "UPDATE Entrega SET estado = ? WHERE id_entrega = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idEntrega);
            ps.executeUpdate();
        }
    }

    /**
     * Marca la entrega como 'Entregada', registra el Pago de saldo y
     * cambia el estado del pedido a 'Entregado' en una sola transacción.
     */
    public void entregarYCobrar(int idEntrega, int idPedido, BigDecimal monto,
                                String metodoPago, String referencia,
                                Integer idEmpleadoCaja) throws SQLException {
        try (Connection c = ConexionBD.get()) {
            c.setAutoCommit(false);
            try {
                pagoDAO.registrar(c, idPedido, "Saldo final", metodoPago, monto, referencia, idEmpleadoCaja);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Entrega SET estado = 'Entregada', fecha_real = ? WHERE id_entrega = ?")) {
                    ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setInt(2, idEntrega);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE Pedido SET estado = 'Entregado', saldo_pendiente = saldo_pendiente - ? WHERE id_pedido = ?")) {
                    ps.setBigDecimal(1, monto);
                    ps.setInt(2, idPedido);
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

    public void asignarRepartidor(int idEntrega, Integer idEmpleado, String direccion, String notas)
            throws SQLException {
        String sql = "UPDATE Entrega SET fk_id_repartidor = ?, direccion = ?, notas = ? WHERE id_entrega = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (idEmpleado == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, idEmpleado);
            if (direccion == null) ps.setNull(2, Types.NVARCHAR); else ps.setString(2, direccion);
            if (notas == null) ps.setNull(3, Types.NVARCHAR); else ps.setString(3, notas);
            ps.setInt(4, idEntrega);
            ps.executeUpdate();
        }
    }

    private Entrega mapear(ResultSet rs) throws SQLException {
        Entrega e = new Entrega();
        e.setIdEntrega(rs.getInt("id_entrega"));
        e.setFkIdPedido(rs.getInt("fk_id_pedido"));
        e.setTipoEntrega(rs.getString("tipo_entrega"));
        e.setDireccion(rs.getString("direccion"));
        Timestamp fp = rs.getTimestamp("fecha_programada");
        if (fp != null) e.setFechaProgramada(fp.toLocalDateTime());
        Timestamp fr = rs.getTimestamp("fecha_real");
        if (fr != null) e.setFechaReal(fr.toLocalDateTime());
        e.setEstado(rs.getString("estado"));
        int idR = rs.getInt("fk_id_repartidor");
        if (!rs.wasNull()) e.setFkIdRepartidor(idR);
        e.setNotas(rs.getString("notas"));
        try { e.setNombreCliente(rs.getString("nombre_cli")); } catch (SQLException ignored) {}
        try { e.setNombreRepartidor(rs.getString("nombre_rep")); } catch (SQLException ignored) {}
        try { e.setSaldoPendiente(rs.getBigDecimal("saldo_pendiente")); } catch (SQLException ignored) {}
        try { e.setEstadoPedido(rs.getString("estado_pedido")); } catch (SQLException ignored) {}
        return e;
    }
}
