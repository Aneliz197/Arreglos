package com.rosato.dao;

import com.rosato.modelo.Empleado;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class EmpleadoDAO {

    public Optional<Empleado> porUsuario(String usuario) throws SQLException {
        String sql = "SELECT * FROM Empleado WHERE usuario = ? AND activo = TRUE";
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
