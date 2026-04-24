package com.rosato.dao;

import com.rosato.modelo.Receta;
import com.rosato.modelo.RecetaIngrediente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RecetaDAO {

    public List<Receta> listar() throws SQLException {
        String sql = "SELECT * FROM Receta ORDER BY tipo_producto";
        List<Receta> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapear(rs));
        }
        return out;
    }

    public Optional<Receta> porTipoProducto(String tipo) throws SQLException {
        String sql = "SELECT * FROM Receta WHERE tipo_producto = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tipo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Receta r = mapear(rs);
                r.getIngredientes().addAll(ingredientesDe(r.getIdReceta()));
                return Optional.of(r);
            }
        }
    }

    public Optional<Receta> porId(int idReceta) throws SQLException {
        String sql = "SELECT * FROM Receta WHERE id_receta = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idReceta);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                Receta r = mapear(rs);
                r.getIngredientes().addAll(ingredientesDe(r.getIdReceta()));
                return Optional.of(r);
            }
        }
    }

    public List<RecetaIngrediente> ingredientesDe(int idReceta) throws SQLException {
        String sql = """
            SELECT ri.*, i.nombre AS nom_ing, i.unidad AS uni_ing
            FROM RecetaIngrediente ri
            JOIN Ingrediente i ON i.id_ingrediente = ri.fk_id_ingrediente
            WHERE ri.fk_id_receta = ?
            ORDER BY i.nombre
            """;
        List<RecetaIngrediente> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idReceta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecetaIngrediente ri = new RecetaIngrediente();
                    ri.setIdRecetaIng(rs.getInt("id_receta_ing"));
                    ri.setFkIdReceta(rs.getInt("fk_id_receta"));
                    ri.setFkIdIngrediente(rs.getInt("fk_id_ingrediente"));
                    ri.setCantidadPorLibra(rs.getBigDecimal("cantidad_por_libra"));
                    ri.setNombreIngrediente(rs.getString("nom_ing"));
                    ri.setUnidad(rs.getString("uni_ing"));
                    out.add(ri);
                }
            }
        }
        return out;
    }

    /** Guarda la receta completa (cabecera + ingredientes) en una sola transacción. */
    public int guardar(Receta r) throws SQLException {
        try (Connection c = ConexionBD.get()) {
            c.setAutoCommit(false);
            try {
                int idReceta;
                if (r.getIdReceta() == null) {
                    idReceta = insertarCabecera(c, r);
                    r.setIdReceta(idReceta);
                } else {
                    idReceta = r.getIdReceta();
                    actualizarCabecera(c, r);
                    try (PreparedStatement del = c.prepareStatement(
                            "DELETE FROM RecetaIngrediente WHERE fk_id_receta = ?")) {
                        del.setInt(1, idReceta);
                        del.executeUpdate();
                    }
                }
                try (PreparedStatement ins = c.prepareStatement("""
                        INSERT INTO RecetaIngrediente (fk_id_receta, fk_id_ingrediente, cantidad_por_libra)
                        VALUES (?, ?, ?)
                        """)) {
                    for (RecetaIngrediente ri : r.getIngredientes()) {
                        ins.setInt(1, idReceta);
                        ins.setInt(2, ri.getFkIdIngrediente());
                        ins.setBigDecimal(3, ri.getCantidadPorLibra());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                c.commit();
                return idReceta;
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            }
        }
    }

    private int insertarCabecera(Connection c, Receta r) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO Receta (tipo_producto, nombre, notas, activa) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getTipoProducto());
            ps.setString(2, r.getNombre());
            if (r.getNotas() == null) ps.setNull(3, Types.NVARCHAR); else ps.setString(3, r.getNotas());
            ps.setBoolean(4, r.isActiva());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
            }
        }
        throw new SQLException("No se pudo insertar la receta.");
    }

    private void actualizarCabecera(Connection c, Receta r) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE Receta SET tipo_producto = ?, nombre = ?, notas = ?, activa = ? WHERE id_receta = ?")) {
            ps.setString(1, r.getTipoProducto());
            ps.setString(2, r.getNombre());
            if (r.getNotas() == null) ps.setNull(3, Types.NVARCHAR); else ps.setString(3, r.getNotas());
            ps.setBoolean(4, r.isActiva());
            ps.setInt(5, r.getIdReceta());
            ps.executeUpdate();
        }
    }

    private Receta mapear(ResultSet rs) throws SQLException {
        Receta r = new Receta();
        r.setIdReceta(rs.getInt("id_receta"));
        r.setTipoProducto(rs.getString("tipo_producto"));
        r.setNombre(rs.getString("nombre"));
        r.setNotas(rs.getString("notas"));
        r.setActiva(rs.getBoolean("activa"));
        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) r.setFechaRegistro(ts.toLocalDateTime());
        return r;
    }
}
