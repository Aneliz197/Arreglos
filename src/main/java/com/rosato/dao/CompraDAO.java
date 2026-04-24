package com.rosato.dao;

import com.rosato.modelo.Compra;
import com.rosato.modelo.DetalleCompra;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompraDAO {

    private final IngredienteDAO ingredienteDAO = new IngredienteDAO();

    public Connection abrirConexion() throws SQLException { return ConexionBD.get(); }

    /**
     * Registra la compra completa en una sola transacción:
     *   - inserta Compra
     *   - inserta cada DetalleCompra
     *   - actualiza stock y costo promedio ponderado de cada ingrediente.
     * Devuelve el id de la compra creada.
     */
    public int registrar(Compra compra, List<DetalleCompra> detalles) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleCompra d : detalles) {
            BigDecimal sub = d.getCantidad().multiply(d.getPrecioUnitario())
                    .setScale(2, RoundingMode.HALF_UP);
            d.setSubtotal(sub);
            total = total.add(sub);
        }
        compra.setTotal(total);

        try (Connection c = ConexionBD.get()) {
            c.setAutoCommit(false);
            try {
                int idCompra = insertarCabecera(c, compra);
                try (PreparedStatement ps = c.prepareStatement("""
                        INSERT INTO DetalleCompra
                          (fk_id_compra, fk_id_ingrediente, cantidad, precio_unitario, subtotal)
                        VALUES (?, ?, ?, ?, ?)
                        """)) {
                    for (DetalleCompra d : detalles) {
                        d.setFkIdCompra(idCompra);
                        ps.setInt(1, idCompra);
                        ps.setInt(2, d.getFkIdIngrediente());
                        ps.setBigDecimal(3, d.getCantidad());
                        ps.setBigDecimal(4, d.getPrecioUnitario());
                        ps.setBigDecimal(5, d.getSubtotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                // Actualizar stock + costo promedio de cada ingrediente
                for (DetalleCompra d : detalles) {
                    ingredienteDAO.aplicarCompra(c, d.getFkIdIngrediente(),
                            d.getCantidad(), d.getPrecioUnitario());
                }
                c.commit();
                compra.setIdCompra(idCompra);
                return idCompra;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private int insertarCabecera(Connection c, Compra compra) throws SQLException {
        String sql = """
            INSERT INTO Compra (fk_id_proveedor, fk_id_empleado, numero_factura, total, observaciones)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, compra.getFkIdProveedor());
            if (compra.getFkIdEmpleado() == null) ps.setNull(2, Types.INTEGER);
            else ps.setInt(2, compra.getFkIdEmpleado());
            if (compra.getNumeroFactura() == null || compra.getNumeroFactura().isBlank())
                ps.setNull(3, Types.NVARCHAR);
            else ps.setString(3, compra.getNumeroFactura());
            ps.setBigDecimal(4, compra.getTotal());
            if (compra.getObservaciones() == null || compra.getObservaciones().isBlank())
                ps.setNull(5, Types.NVARCHAR);
            else ps.setString(5, compra.getObservaciones());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
            }
        }
        throw new SQLException("No se pudo insertar la compra.");
    }

    public List<Compra> listarTodas() throws SQLException {
        String sql = """
            SELECT c.*, p.nombre AS nombre_prov
            FROM Compra c
            JOIN Proveedor p ON p.id_proveedor = c.fk_id_proveedor
            ORDER BY c.fecha_compra DESC
            """;
        List<Compra> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapear(rs));
        }
        return out;
    }

    public List<DetalleCompra> detallesDe(int idCompra) throws SQLException {
        String sql = """
            SELECT d.*, i.nombre AS nom_ing, i.unidad AS uni_ing
            FROM DetalleCompra d
            JOIN Ingrediente i ON i.id_ingrediente = d.fk_id_ingrediente
            WHERE d.fk_id_compra = ?
            """;
        List<DetalleCompra> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCompra);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetalleCompra d = new DetalleCompra();
                    d.setIdDetalle(rs.getInt("id_detalle"));
                    d.setFkIdCompra(rs.getInt("fk_id_compra"));
                    d.setFkIdIngrediente(rs.getInt("fk_id_ingrediente"));
                    d.setCantidad(rs.getBigDecimal("cantidad"));
                    d.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
                    d.setSubtotal(rs.getBigDecimal("subtotal"));
                    d.setNombreIngrediente(rs.getString("nom_ing"));
                    d.setUnidad(rs.getString("uni_ing"));
                    out.add(d);
                }
            }
        }
        return out;
    }

    private Compra mapear(ResultSet rs) throws SQLException {
        Compra c = new Compra();
        c.setIdCompra(rs.getInt("id_compra"));
        c.setFkIdProveedor(rs.getInt("fk_id_proveedor"));
        int fkEmp = rs.getInt("fk_id_empleado");
        c.setFkIdEmpleado(rs.wasNull() ? null : fkEmp);
        Timestamp ts = rs.getTimestamp("fecha_compra");
        if (ts != null) c.setFechaCompra(ts.toLocalDateTime());
        c.setNumeroFactura(rs.getString("numero_factura"));
        c.setTotal(rs.getBigDecimal("total"));
        c.setObservaciones(rs.getString("observaciones"));
        try { c.setNombreProveedor(rs.getString("nombre_prov")); } catch (SQLException ignored) { }
        return c;
    }
}
