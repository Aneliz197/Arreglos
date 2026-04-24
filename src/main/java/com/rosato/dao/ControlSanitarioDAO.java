package com.rosato.dao;

import com.rosato.modelo.ControlSanitarioEmpleado;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ControlSanitarioDAO {

    public List<ControlSanitarioEmpleado> listar(LocalDate desde, LocalDate hasta, Integer idEmpleado)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT cs.*, e.nombre_completo, e.area_trabajo
              FROM ControlSanitarioEmpleado cs
              JOIN Empleado e ON e.id_empleado = cs.fk_id_empleado
             WHERE 1 = 1
            """);
        if (desde != null)        sql.append(" AND cs.fecha >= ?");
        if (hasta != null)        sql.append(" AND cs.fecha <= ?");
        if (idEmpleado != null)   sql.append(" AND cs.fk_id_empleado = ?");
        sql.append(" ORDER BY cs.fecha DESC, e.nombre_completo");

        List<ControlSanitarioEmpleado> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (desde != null)      ps.setDate(idx++, Date.valueOf(desde));
            if (hasta != null)      ps.setDate(idx++, Date.valueOf(hasta));
            if (idEmpleado != null) ps.setInt(idx, idEmpleado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public int insertar(ControlSanitarioEmpleado c) throws SQLException {
        String sql = """
            INSERT INTO ControlSanitarioEmpleado
              (fecha, fk_id_empleado, uniforme_ok, manos_ok, redecilla_ok, guantes_ok, unas_ok, salud_ok, observaciones)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection cx = ConexionBD.get();
             PreparedStatement ps = cx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, Date.valueOf(c.getFecha()));
            ps.setInt(2, c.getFkIdEmpleado());
            ps.setBoolean(3, c.isUniformeOk());
            ps.setBoolean(4, c.isManosOk());
            ps.setBoolean(5, c.isRedecillaOk());
            ps.setBoolean(6, c.isGuantesOk());
            ps.setBoolean(7, c.isUnasOk());
            ps.setBoolean(8, c.isSaludOk());
            if (c.getObservaciones() == null) ps.setNull(9, Types.NVARCHAR);
            else ps.setString(9, c.getObservaciones());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) { c.setIdControl(k.getInt(1)); return c.getIdControl(); }
            }
        }
        throw new SQLException("No se pudo registrar el control sanitario.");
    }

    private ControlSanitarioEmpleado mapear(ResultSet rs) throws SQLException {
        ControlSanitarioEmpleado c = new ControlSanitarioEmpleado();
        c.setIdControl(rs.getInt("id_control"));
        Date f = rs.getDate("fecha");
        if (f != null) c.setFecha(f.toLocalDate());
        c.setFkIdEmpleado(rs.getInt("fk_id_empleado"));
        c.setUniformeOk(rs.getBoolean("uniforme_ok"));
        c.setManosOk(rs.getBoolean("manos_ok"));
        c.setRedecillaOk(rs.getBoolean("redecilla_ok"));
        c.setGuantesOk(rs.getBoolean("guantes_ok"));
        c.setUnasOk(rs.getBoolean("unas_ok"));
        c.setSaludOk(rs.getBoolean("salud_ok"));
        c.setObservaciones(rs.getString("observaciones"));
        Timestamp fr = rs.getTimestamp("fecha_registro");
        if (fr != null) c.setFechaRegistro(fr.toLocalDateTime());
        c.setNombreEmpleado(rs.getString("nombre_completo"));
        c.setAreaEmpleado(rs.getString("area_trabajo"));
        return c;
    }
}
