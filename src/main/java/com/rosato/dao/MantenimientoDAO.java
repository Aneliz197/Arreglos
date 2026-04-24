package com.rosato.dao;

import com.rosato.modelo.Mantenimiento;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MantenimientoDAO {

    private final MaquinaDAO maquinaDAO = new MaquinaDAO();

    public List<Mantenimiento> listar(Integer idMaquina, LocalDate desde, LocalDate hasta)
            throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT m.*, mq.codigo AS cod_maq, mq.nombre AS nom_maq,
                   emp.nombre_completo AS nom_emp
            FROM Mantenimiento m
            JOIN Maquina mq ON mq.id_maquina = m.fk_id_maquina
            LEFT JOIN Empleado emp ON emp.id_empleado = m.fk_id_empleado
            WHERE 1 = 1
            """);
        if (idMaquina != null) sql.append(" AND m.fk_id_maquina = ?");
        if (desde != null)     sql.append(" AND m.fecha >= ?");
        if (hasta != null)     sql.append(" AND m.fecha <= ?");
        sql.append(" ORDER BY m.fecha DESC, m.id_mantenimiento DESC");

        List<Mantenimiento> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (idMaquina != null) ps.setInt(idx++, idMaquina);
            if (desde != null)     ps.setDate(idx++, Date.valueOf(desde));
            if (hasta != null)     ps.setDate(idx, Date.valueOf(hasta));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    /**
     * Inserta el mantenimiento y, en la misma transacción, actualiza el
     * estado y la próxima revisión de la máquina:
     *  - Si el mantenimiento es correctivo, el estado final es 'Operativa'
     *    (se asume que el correctivo deja la máquina reparada).
     *  - Si es preventivo, queda 'Operativa' igualmente.
     *  - proxima_revision se copia al de la máquina.
     */
    public void registrar(Mantenimiento m) throws SQLException {
        try (Connection c = ConexionBD.get()) {
            c.setAutoCommit(false);
            try {
                String sql = """
                    INSERT INTO Mantenimiento (fk_id_maquina, tipo, fecha, tecnico,
                                               costo, descripcion, proxima_revision, fk_id_empleado)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                    """;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, m.getFkIdMaquina());
                    ps.setString(2, m.getTipo());
                    ps.setDate(3, Date.valueOf(m.getFecha()));
                    if (m.getTecnico() == null) ps.setNull(4, Types.NVARCHAR); else ps.setString(4, m.getTecnico());
                    ps.setBigDecimal(5, m.getCosto());
                    if (m.getDescripcion() == null) ps.setNull(6, Types.NVARCHAR); else ps.setString(6, m.getDescripcion());
                    if (m.getProximaRevision() == null) ps.setNull(7, Types.DATE);
                    else ps.setDate(7, Date.valueOf(m.getProximaRevision()));
                    if (m.getFkIdEmpleado() == null) ps.setNull(8, Types.INTEGER);
                    else ps.setInt(8, m.getFkIdEmpleado());
                    ps.executeUpdate();
                    try (ResultSet k = ps.getGeneratedKeys()) {
                        if (k.next()) m.setIdMantenimiento(k.getInt(1));
                    }
                }
                maquinaDAO.actualizarEstadoYRevision(c, m.getFkIdMaquina(),
                        "Operativa", m.getProximaRevision());
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                if (ex instanceof SQLException se) throw se;
                throw new SQLException(ex);
            }
        }
    }

    private Mantenimiento mapear(ResultSet rs) throws SQLException {
        Mantenimiento m = new Mantenimiento();
        m.setIdMantenimiento(rs.getInt("id_mantenimiento"));
        m.setFkIdMaquina(rs.getInt("fk_id_maquina"));
        m.setTipo(rs.getString("tipo"));
        Date f = rs.getDate("fecha");
        if (f != null) m.setFecha(f.toLocalDate());
        m.setTecnico(rs.getString("tecnico"));
        m.setCosto(rs.getBigDecimal("costo"));
        m.setDescripcion(rs.getString("descripcion"));
        Date pr = rs.getDate("proxima_revision");
        if (pr != null) m.setProximaRevision(pr.toLocalDate());
        int ide = rs.getInt("fk_id_empleado");
        if (!rs.wasNull()) m.setFkIdEmpleado(ide);
        Timestamp frg = rs.getTimestamp("fecha_registro");
        if (frg != null) m.setFechaRegistro(frg.toLocalDateTime());
        try { m.setCodigoMaquina(rs.getString("cod_maq")); } catch (SQLException ignored) {}
        try { m.setNombreMaquina(rs.getString("nom_maq")); } catch (SQLException ignored) {}
        try { m.setNombreEmpleado(rs.getString("nom_emp")); } catch (SQLException ignored) {}
        return m;
    }
}
