package com.rosato.dao;

import com.rosato.modelo.Pedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO básico de Pedido (Módulo 2). Funcionalidad mínima para el listado. */
public class PedidoDAO {

    public List<Pedido> listarTodos() throws SQLException {
        String sql = """
            SELECT p.*, (c.nombre || ' ' || c.apellido) AS nombre_cli
            FROM Pedido p
            JOIN Cliente c ON c.id_cliente = p.fk_id_cliente
            ORDER BY p.fecha_pedido DESC
            """;
        // MySQL no soporta '||', usamos CONCAT
        sql = """
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
