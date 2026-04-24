package com.rosato.dao;

import com.rosato.modelo.Proveedor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProveedorDAO {

    public Optional<Proveedor> porId(int id) throws SQLException {
        String sql = "SELECT * FROM Proveedor WHERE id_proveedor = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Proveedor> listar(String filtro) throws SQLException {
        String sql = """
            SELECT * FROM Proveedor
            WHERE activo = 1
              AND (nombre LIKE ? OR telefono LIKE ? OR ISNULL(rnc, '') LIKE ?)
            ORDER BY nombre
            """;
        String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
        List<Proveedor> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public int insertar(Proveedor p) throws SQLException {
        String sql = """
            INSERT INTO Proveedor (nombre, rnc, telefono, email, direccion, activo)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNombre());
            setOrNull(ps, 2, p.getRnc());
            ps.setString(3, p.getTelefono());
            setOrNull(ps, 4, p.getEmail());
            setOrNull(ps, 5, p.getDireccion());
            ps.setBoolean(6, p.isActivo());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) {
                    p.setIdProveedor(k.getInt(1));
                    return p.getIdProveedor();
                }
            }
        }
        throw new SQLException("No se pudo insertar el proveedor.");
    }

    public void actualizar(Proveedor p) throws SQLException {
        String sql = """
            UPDATE Proveedor SET nombre = ?, rnc = ?, telefono = ?, email = ?,
                                 direccion = ?, activo = ?
             WHERE id_proveedor = ?
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNombre());
            setOrNull(ps, 2, p.getRnc());
            ps.setString(3, p.getTelefono());
            setOrNull(ps, 4, p.getEmail());
            setOrNull(ps, 5, p.getDireccion());
            ps.setBoolean(6, p.isActivo());
            ps.setInt(7, p.getIdProveedor());
            ps.executeUpdate();
        }
    }

    private void setOrNull(PreparedStatement ps, int idx, String val) throws SQLException {
        if (val == null || val.isBlank()) ps.setNull(idx, Types.NVARCHAR);
        else ps.setString(idx, val);
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setIdProveedor(rs.getInt("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        p.setRnc(rs.getString("rnc"));
        p.setTelefono(rs.getString("telefono"));
        p.setEmail(rs.getString("email"));
        p.setDireccion(rs.getString("direccion"));
        p.setActivo(rs.getBoolean("activo"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) p.setFechaRegistro(ts.toLocalDateTime());
        return p;
    }
}
