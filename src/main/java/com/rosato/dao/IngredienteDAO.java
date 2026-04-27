package com.rosato.dao;

import com.rosato.modelo.Ingrediente;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IngredienteDAO {

    public Optional<Ingrediente> porId(int id) throws SQLException {
        String sql = "SELECT * FROM Ingrediente WHERE id_ingrediente = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Ingrediente> listar(String filtro) throws SQLException {
        String sql = "SELECT * FROM Ingrediente WHERE activo = 1 AND nombre LIKE ? ORDER BY nombre";
        List<Ingrediente> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + (filtro == null ? "" : filtro.trim()) + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public int insertar(Ingrediente i) throws SQLException {
        String sql = """
            INSERT INTO Ingrediente (nombre, unidad, stock_actual, stock_minimo, costo_promedio, activo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getUnidad());
            ps.setBigDecimal(3, i.getStockActual());
            ps.setBigDecimal(4, i.getStockMinimo());
            ps.setBigDecimal(5, i.getCostoPromedio());
            ps.setBoolean(6, i.isActivo());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) {
                    i.setIdIngrediente(k.getInt(1));
                    return i.getIdIngrediente();
                }
            }
        }
        throw new SQLException("No se pudo insertar el ingrediente.");
    }

    public void actualizar(Ingrediente i) throws SQLException {
        String sql = """
            UPDATE Ingrediente SET nombre = ?, unidad = ?, stock_actual = ?, stock_minimo = ?,
                   costo_promedio = ?, activo = ?
             WHERE id_ingrediente = ?
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, i.getNombre());
            ps.setString(2, i.getUnidad());
            ps.setBigDecimal(3, i.getStockActual());
            ps.setBigDecimal(4, i.getStockMinimo());
            ps.setBigDecimal(5, i.getCostoPromedio());
            ps.setBoolean(6, i.isActivo());
            ps.setInt(7, i.getIdIngrediente());
            ps.executeUpdate();
        }
    }

    /**
     * Aplica una compra al inventario: aumenta stock y actualiza el costo
     * promedio ponderado.  Pensado para usarse dentro de una transacción
     * (se pasa la Connection externa).
     */
    public void aplicarCompra(Connection c, int idIngrediente,
                              BigDecimal cantidad, BigDecimal precioUnitario) throws SQLException {
        String sel = "SELECT stock_actual, costo_promedio FROM Ingrediente WHERE id_ingrediente = ?";
        BigDecimal stockPrev;
        BigDecimal costoPrev;
        try (PreparedStatement ps = c.prepareStatement(sel)) {
            ps.setInt(1, idIngrediente);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Ingrediente no existe: " + idIngrediente);
                stockPrev = rs.getBigDecimal("stock_actual");
                costoPrev = rs.getBigDecimal("costo_promedio");
            }
        }
        BigDecimal nuevoStock = stockPrev.add(cantidad);
        BigDecimal nuevoCosto = com.rosato.util.InventarioCalc.nuevoCostoPromedio(
                stockPrev, costoPrev, cantidad, precioUnitario);
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE Ingrediente SET stock_actual = ?, costo_promedio = ? WHERE id_ingrediente = ?")) {
            ps.setBigDecimal(1, nuevoStock);
            ps.setBigDecimal(2, nuevoCosto);
            ps.setInt(3, idIngrediente);
            ps.executeUpdate();
        }
    }

    private Ingrediente mapear(ResultSet rs) throws SQLException {
        Ingrediente i = new Ingrediente();
        i.setIdIngrediente(rs.getInt("id_ingrediente"));
        i.setNombre(rs.getString("nombre"));
        i.setUnidad(rs.getString("unidad"));
        i.setStockActual(rs.getBigDecimal("stock_actual"));
        i.setStockMinimo(rs.getBigDecimal("stock_minimo"));
        i.setCostoPromedio(rs.getBigDecimal("costo_promedio"));
        i.setActivo(rs.getBoolean("activo"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) i.setFechaRegistro(ts.toLocalDateTime());
        return i;
    }
}
