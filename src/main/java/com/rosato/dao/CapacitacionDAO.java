package com.rosato.dao;

import com.rosato.modelo.Capacitacion;
import com.rosato.modelo.CapacitacionEmpleado;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CapacitacionDAO {

    public Optional<Capacitacion> porId(int id) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM Capacitacion WHERE id_capacitacion = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapear(rs)) : Optional.empty();
            }
        }
    }

    public List<Capacitacion> listar(LocalDate desde, LocalDate hasta) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM Capacitacion WHERE 1 = 1");
        if (desde != null) sql.append(" AND fecha >= ?");
        if (hasta != null) sql.append(" AND fecha <= ?");
        sql.append(" ORDER BY fecha DESC, id_capacitacion DESC");

        List<Capacitacion> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (desde != null) ps.setDate(idx++, Date.valueOf(desde));
            if (hasta != null) ps.setDate(idx, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public int insertar(Capacitacion c) throws SQLException {
        String sql = """
            INSERT INTO Capacitacion (titulo, fecha, instructor, horas, ubicacion, descripcion)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (Connection cx = ConexionBD.get();
             PreparedStatement ps = cx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, c);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) { c.setIdCapacitacion(k.getInt(1)); return c.getIdCapacitacion(); }
            }
        }
        throw new SQLException("No se pudo insertar la capacitación.");
    }

    public void actualizar(Capacitacion c) throws SQLException {
        String sql = """
            UPDATE Capacitacion SET titulo = ?, fecha = ?, instructor = ?, horas = ?,
                                    ubicacion = ?, descripcion = ?
             WHERE id_capacitacion = ?
            """;
        try (Connection cx = ConexionBD.get();
             PreparedStatement ps = cx.prepareStatement(sql)) {
            bind(ps, c);
            ps.setInt(7, c.getIdCapacitacion());
            ps.executeUpdate();
        }
    }

    public List<CapacitacionEmpleado> listarAsistentes(int idCapacitacion) throws SQLException {
        String sql = """
            SELECT ce.*, e.nombre_completo, e.area_trabajo
            FROM CapacitacionEmpleado ce
            JOIN Empleado e ON e.id_empleado = ce.fk_id_empleado
            WHERE ce.fk_id_capacitacion = ?
            ORDER BY e.nombre_completo
            """;
        List<CapacitacionEmpleado> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCapacitacion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CapacitacionEmpleado ce = new CapacitacionEmpleado();
                    ce.setIdCapacitacionEmp(rs.getInt("id_capacitacion_emp"));
                    ce.setFkIdCapacitacion(rs.getInt("fk_id_capacitacion"));
                    ce.setFkIdEmpleado(rs.getInt("fk_id_empleado"));
                    ce.setAsistio(rs.getBoolean("asistio"));
                    ce.setCalificacion(rs.getBigDecimal("calificacion"));
                    ce.setObservaciones(rs.getString("observaciones"));
                    ce.setNombreEmpleado(rs.getString("nombre_completo"));
                    ce.setAreaEmpleado(rs.getString("area_trabajo"));
                    out.add(ce);
                }
            }
        }
        return out;
    }

    public int agregarAsistente(int idCapacitacion, int idEmpleado) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO CapacitacionEmpleado (fk_id_capacitacion, fk_id_empleado, asistio) VALUES (?, ?, 0)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCapacitacion);
            ps.setInt(2, idEmpleado);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
            }
        }
        return -1;
    }

    public void actualizarAsistente(CapacitacionEmpleado ce) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "UPDATE CapacitacionEmpleado SET asistio = ?, calificacion = ?, observaciones = ? WHERE id_capacitacion_emp = ?")) {
            ps.setBoolean(1, ce.isAsistio());
            if (ce.getCalificacion() == null) ps.setNull(2, Types.DECIMAL);
            else ps.setBigDecimal(2, ce.getCalificacion());
            if (ce.getObservaciones() == null) ps.setNull(3, Types.NVARCHAR);
            else ps.setString(3, ce.getObservaciones());
            ps.setInt(4, ce.getIdCapacitacionEmp());
            ps.executeUpdate();
        }
    }

    public void quitarAsistente(int idCapacitacionEmp) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "DELETE FROM CapacitacionEmpleado WHERE id_capacitacion_emp = ?")) {
            ps.setInt(1, idCapacitacionEmp);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, Capacitacion c) throws SQLException {
        ps.setString(1, c.getTitulo());
        ps.setDate(2, Date.valueOf(c.getFecha()));
        if (c.getInstructor() == null) ps.setNull(3, Types.NVARCHAR); else ps.setString(3, c.getInstructor());
        ps.setBigDecimal(4, c.getHoras());
        if (c.getUbicacion() == null) ps.setNull(5, Types.NVARCHAR); else ps.setString(5, c.getUbicacion());
        if (c.getDescripcion() == null) ps.setNull(6, Types.NVARCHAR); else ps.setString(6, c.getDescripcion());
    }

    private Capacitacion mapear(ResultSet rs) throws SQLException {
        Capacitacion c = new Capacitacion();
        c.setIdCapacitacion(rs.getInt("id_capacitacion"));
        c.setTitulo(rs.getString("titulo"));
        Date f = rs.getDate("fecha");
        if (f != null) c.setFecha(f.toLocalDate());
        c.setInstructor(rs.getString("instructor"));
        c.setHoras(rs.getBigDecimal("horas"));
        c.setUbicacion(rs.getString("ubicacion"));
        c.setDescripcion(rs.getString("descripcion"));
        Timestamp fr = rs.getTimestamp("fecha_registro");
        if (fr != null) c.setFechaRegistro(fr.toLocalDateTime());
        return c;
    }
}
