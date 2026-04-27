package com.rosato.dao;

import com.rosato.modelo.Empleado;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class EmpleadoDAO {

    public java.util.List<Empleado> listarActivos() throws SQLException {
        java.util.List<Empleado> out = new java.util.ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM Empleado WHERE activo = 1 ORDER BY nombre_completo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(mapear(rs));
        }
        return out;
    }

    public java.util.List<Empleado> listar(String filtro, String area, Boolean soloActivos)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM Empleado WHERE 1 = 1");
        if (filtro != null && !filtro.isBlank())
            sql.append(" AND (nombre_completo LIKE ? OR usuario LIKE ? OR cedula LIKE ?)");
        if (area != null && !area.isBlank() && !"Todas".equalsIgnoreCase(area))
            sql.append(" AND area_trabajo = ?");
        if (Boolean.TRUE.equals(soloActivos)) sql.append(" AND activo = 1");
        sql.append(" ORDER BY nombre_completo");

        java.util.List<Empleado> out = new java.util.ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (filtro != null && !filtro.isBlank()) {
                String like = "%" + filtro.trim() + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (area != null && !area.isBlank() && !"Todas".equalsIgnoreCase(area)) {
                ps.setString(idx, area);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public Optional<Empleado> porId(int id) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM Empleado WHERE id_empleado = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public int insertar(Empleado e) throws SQLException {
        String sql = """
            INSERT INTO Empleado
              (nombre_completo, cedula, telefono, usuario, contrasena, area_trabajo,
               experiencia, disponibilidad, salario, fecha_contratacion, fecha_nacimiento,
               fecha_prueba_embarazo, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, e);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) { e.setIdEmpleado(k.getInt(1)); return e.getIdEmpleado(); }
            }
        }
        throw new SQLException("No se pudo insertar el empleado.");
    }

    public void actualizar(Empleado e) throws SQLException {
        String sql = """
            UPDATE Empleado SET
              nombre_completo = ?, cedula = ?, telefono = ?, usuario = ?, contrasena = ?,
              area_trabajo = ?, experiencia = ?, disponibilidad = ?, salario = ?,
              fecha_contratacion = ?, fecha_nacimiento = ?, fecha_prueba_embarazo = ?, activo = ?
             WHERE id_empleado = ?
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, e);
            ps.setInt(14, e.getIdEmpleado());
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Empleado e) throws SQLException {
        ps.setString(1, e.getNombreCompleto());
        if (e.getCedula() == null)    ps.setNull(2, Types.NVARCHAR); else ps.setString(2, e.getCedula());
        if (e.getTelefono() == null)  ps.setNull(3, Types.NVARCHAR); else ps.setString(3, e.getTelefono());
        ps.setString(4, e.getUsuario());
        ps.setString(5, e.getContrasena());
        if (e.getAreaTrabajo() == null)   ps.setNull(6, Types.NVARCHAR); else ps.setString(6, e.getAreaTrabajo());
        if (e.getExperiencia() == null)   ps.setNull(7, Types.NVARCHAR); else ps.setString(7, e.getExperiencia());
        if (e.getDisponibilidad() == null) ps.setNull(8, Types.NVARCHAR); else ps.setString(8, e.getDisponibilidad());
        if (e.getSalario() == null) ps.setNull(9, Types.DECIMAL); else ps.setDouble(9, e.getSalario());
        if (e.getFechaContratacion() == null) ps.setNull(10, Types.DATE);
        else ps.setDate(10, Date.valueOf(e.getFechaContratacion()));
        if (e.getFechaNacimiento() == null) ps.setNull(11, Types.DATE);
        else ps.setDate(11, Date.valueOf(e.getFechaNacimiento()));
        if (e.getFechaPruebaEmbarazo() == null) ps.setNull(12, Types.DATE);
        else ps.setDate(12, Date.valueOf(e.getFechaPruebaEmbarazo()));
        ps.setBoolean(13, e.isActivo());
    }

    public Optional<Empleado> porUsuario(String usuario) throws SQLException {
        String sql = "SELECT * FROM Empleado WHERE usuario = ? AND activo = 1";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public void registrarIntentoFallido(int id) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE Empleado SET intentos_fallidos = intentos_fallidos + 1 WHERE id_empleado = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void bloquearHasta(int id, LocalDateTime hasta) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE Empleado SET bloqueado_hasta = ? WHERE id_empleado = ?")) {
            ps.setTimestamp(1, Timestamp.valueOf(hasta));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void reiniciarIntentos(int id) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE Empleado SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id_empleado = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setIdEmpleado(rs.getInt("id_empleado"));
        e.setNombreCompleto(rs.getString("nombre_completo"));
        e.setCedula(rs.getString("cedula"));
        e.setTelefono(rs.getString("telefono"));
        e.setUsuario(rs.getString("usuario"));
        e.setContrasena(rs.getString("contrasena"));
        e.setAreaTrabajo(rs.getString("area_trabajo"));
        e.setExperiencia(rs.getString("experiencia"));
        e.setDisponibilidad(rs.getString("disponibilidad"));
        double salario = rs.getDouble("salario");
        if (!rs.wasNull()) e.setSalario(salario);
        Date fc = rs.getDate("fecha_contratacion");
        if (fc != null) e.setFechaContratacion(fc.toLocalDate());
        Date fn = rs.getDate("fecha_nacimiento");
        if (fn != null) e.setFechaNacimiento(fn.toLocalDate());
        Date fp = rs.getDate("fecha_prueba_embarazo");
        if (fp != null) e.setFechaPruebaEmbarazo(fp.toLocalDate());
        e.setActivo(rs.getBoolean("activo"));
        e.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        Timestamp bh = rs.getTimestamp("bloqueado_hasta");
        if (bh != null) e.setBloqueadoHasta(bh.toLocalDateTime());
        return e;
    }
}
