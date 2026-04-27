package com.rosato.dao;

import com.rosato.modelo.Maquina;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MaquinaDAO {

    public Optional<Maquina> porId(int id) throws SQLException {
        String sql = "SELECT * FROM Maquina WHERE id_maquina = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Maquina> listar(String filtro, String estado) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM Maquina WHERE activo = 1");
        if (filtro != null && !filtro.isBlank()) sql.append(" AND (nombre LIKE ? OR codigo LIKE ?)");
        if (estado != null && !estado.isBlank() && !"Todos".equalsIgnoreCase(estado)) sql.append(" AND estado = ?");
        sql.append(" ORDER BY codigo");
        List<Maquina> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (filtro != null && !filtro.isBlank()) {
                String like = "%" + filtro.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (estado != null && !estado.isBlank() && !"Todos".equalsIgnoreCase(estado)) {
                ps.setString(idx, estado);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public List<Maquina> listarActivas() throws SQLException {
        return listar(null, null);
    }

    public int insertar(Maquina m) throws SQLException {
        String sql = """
            INSERT INTO Maquina (codigo, nombre, tipo, marca, modelo, serial,
                                 fecha_adquisicion, estado, proxima_revision,
                                 ubicacion, notas, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, m);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) { m.setIdMaquina(k.getInt(1)); return m.getIdMaquina(); }
            }
        }
        throw new SQLException("No se pudo insertar la máquina.");
    }

    public void actualizar(Maquina m) throws SQLException {
        String sql = """
            UPDATE Maquina SET codigo = ?, nombre = ?, tipo = ?, marca = ?, modelo = ?,
                   serial = ?, fecha_adquisicion = ?, estado = ?, proxima_revision = ?,
                   ubicacion = ?, notas = ?, activo = ?
             WHERE id_maquina = ?
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, m);
            ps.setInt(13, m.getIdMaquina());
            ps.executeUpdate();
        }
    }

    /** Actualiza estado + proxima_revision dentro de una transacción externa. */
    public void actualizarEstadoYRevision(Connection c, int id, String estado,
                                          LocalDate proximaRevision) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE Maquina SET estado = ?, proxima_revision = ? WHERE id_maquina = ?")) {
            ps.setString(1, estado);
            if (proximaRevision == null) ps.setNull(2, Types.DATE);
            else ps.setDate(2, Date.valueOf(proximaRevision));
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Maquina m) throws SQLException {
        ps.setString(1, m.getCodigo());
        ps.setString(2, m.getNombre());
        ps.setString(3, m.getTipo());
        if (m.getMarca() == null)  ps.setNull(4, Types.NVARCHAR); else ps.setString(4, m.getMarca());
        if (m.getModelo() == null) ps.setNull(5, Types.NVARCHAR); else ps.setString(5, m.getModelo());
        if (m.getSerial() == null) ps.setNull(6, Types.NVARCHAR); else ps.setString(6, m.getSerial());
        if (m.getFechaAdquisicion() == null) ps.setNull(7, Types.DATE);
        else ps.setDate(7, Date.valueOf(m.getFechaAdquisicion()));
        ps.setString(8, m.getEstado() == null ? "Operativa" : m.getEstado());
        if (m.getProximaRevision() == null) ps.setNull(9, Types.DATE);
        else ps.setDate(9, Date.valueOf(m.getProximaRevision()));
        if (m.getUbicacion() == null) ps.setNull(10, Types.NVARCHAR); else ps.setString(10, m.getUbicacion());
        if (m.getNotas() == null) ps.setNull(11, Types.NVARCHAR); else ps.setString(11, m.getNotas());
        ps.setBoolean(12, m.isActivo());
    }

    private Maquina mapear(ResultSet rs) throws SQLException {
        Maquina m = new Maquina();
        m.setIdMaquina(rs.getInt("id_maquina"));
        m.setCodigo(rs.getString("codigo"));
        m.setNombre(rs.getString("nombre"));
        m.setTipo(rs.getString("tipo"));
        m.setMarca(rs.getString("marca"));
        m.setModelo(rs.getString("modelo"));
        m.setSerial(rs.getString("serial"));
        Date fa = rs.getDate("fecha_adquisicion");
        if (fa != null) m.setFechaAdquisicion(fa.toLocalDate());
        m.setEstado(rs.getString("estado"));
        Date pr = rs.getDate("proxima_revision");
        if (pr != null) m.setProximaRevision(pr.toLocalDate());
        m.setUbicacion(rs.getString("ubicacion"));
        m.setNotas(rs.getString("notas"));
        m.setActivo(rs.getBoolean("activo"));
        Timestamp fr = rs.getTimestamp("fecha_registro");
        if (fr != null) m.setFechaRegistro(fr.toLocalDateTime());
        return m;
    }
}
