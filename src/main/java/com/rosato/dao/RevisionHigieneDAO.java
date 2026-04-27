package com.rosato.dao;

import com.rosato.modelo.RevisionHigiene;
import com.rosato.modelo.RevisionHigieneItem;
import com.rosato.util.HigieneCalc;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RevisionHigieneDAO {

    public Optional<RevisionHigiene> porId(int id) throws SQLException {
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM RevisionHigiene WHERE id_revision = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                RevisionHigiene r = mapear(rs);
                r.setItems(listarItems(id));
                return Optional.of(r);
            }
        }
    }

    public List<RevisionHigiene> listar(LocalDate desde, LocalDate hasta, String area)
            throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM RevisionHigiene WHERE 1 = 1");
        if (desde != null) sql.append(" AND fecha >= ?");
        if (hasta != null) sql.append(" AND fecha <= ?");
        if (area != null && !area.isBlank() && !"Todas".equalsIgnoreCase(area))
            sql.append(" AND area = ?");
        sql.append(" ORDER BY fecha DESC, id_revision DESC");

        List<RevisionHigiene> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int idx = 1;
            if (desde != null) ps.setDate(idx++, Date.valueOf(desde));
            if (hasta != null) ps.setDate(idx++, Date.valueOf(hasta));
            if (area != null && !area.isBlank() && !"Todas".equalsIgnoreCase(area))
                ps.setString(idx, area);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public List<RevisionHigieneItem> listarItems(int idRevision) throws SQLException {
        List<RevisionHigieneItem> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(
                     "SELECT * FROM RevisionHigieneItem WHERE fk_id_revision = ? ORDER BY id_item")) {
            ps.setInt(1, idRevision);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RevisionHigieneItem it = new RevisionHigieneItem();
                    it.setIdItem(rs.getInt("id_item"));
                    it.setFkIdRevision(rs.getInt("fk_id_revision"));
                    it.setDescripcion(rs.getString("descripcion"));
                    it.setCumple(rs.getBoolean("cumple"));
                    it.setObservacion(rs.getString("observacion"));
                    out.add(it);
                }
            }
        }
        return out;
    }

    public int guardar(RevisionHigiene r) throws SQLException {
        recalcular(r);
        try (Connection c = ConexionBD.get()) {
            c.setAutoCommit(false);
            try {
                if (r.getIdRevision() == null) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO RevisionHigiene (fecha, area, responsable, fk_id_empleado, puntaje, estado, observaciones) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        bind(ps, r);
                        ps.executeUpdate();
                        try (ResultSet k = ps.getGeneratedKeys()) {
                            if (k.next()) r.setIdRevision(k.getInt(1));
                        }
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE RevisionHigiene SET fecha = ?, area = ?, responsable = ?, fk_id_empleado = ?, " +
                                    "puntaje = ?, estado = ?, observaciones = ? WHERE id_revision = ?")) {
                        bind(ps, r);
                        ps.setInt(8, r.getIdRevision());
                        ps.executeUpdate();
                    }
                    try (PreparedStatement del = c.prepareStatement(
                            "DELETE FROM RevisionHigieneItem WHERE fk_id_revision = ?")) {
                        del.setInt(1, r.getIdRevision());
                        del.executeUpdate();
                    }
                }
                try (PreparedStatement ins = c.prepareStatement(
                        "INSERT INTO RevisionHigieneItem (fk_id_revision, descripcion, cumple, observacion) " +
                                "VALUES (?, ?, ?, ?)")) {
                    for (RevisionHigieneItem it : r.getItems()) {
                        ins.setInt(1, r.getIdRevision());
                        ins.setString(2, it.getDescripcion());
                        ins.setBoolean(3, it.isCumple());
                        if (it.getObservacion() == null || it.getObservacion().isBlank()) ins.setNull(4, Types.NVARCHAR);
                        else ins.setString(4, it.getObservacion().trim());
                        ins.addBatch();
                    }
                    ins.executeBatch();
                }
                c.commit();
                return r.getIdRevision();
            } catch (SQLException ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void recalcular(RevisionHigiene r) {
        int total = r.getItems() == null ? 0 : r.getItems().size();
        int ok = 0;
        if (total > 0) {
            for (RevisionHigieneItem it : r.getItems()) if (it.isCumple()) ok++;
        }
        BigDecimal pct = HigieneCalc.porcentaje(ok, total);
        r.setPuntaje(pct);
        r.setEstado(HigieneCalc.estadoLabel(HigieneCalc.evaluar(pct)));
    }

    private void bind(PreparedStatement ps, RevisionHigiene r) throws SQLException {
        ps.setDate(1, Date.valueOf(r.getFecha()));
        ps.setString(2, r.getArea());
        if (r.getResponsable() == null) ps.setNull(3, Types.NVARCHAR); else ps.setString(3, r.getResponsable());
        if (r.getFkIdEmpleado() == null) ps.setNull(4, Types.INTEGER); else ps.setInt(4, r.getFkIdEmpleado());
        ps.setBigDecimal(5, r.getPuntaje());
        ps.setString(6, r.getEstado());
        if (r.getObservaciones() == null) ps.setNull(7, Types.NVARCHAR); else ps.setString(7, r.getObservaciones());
    }

    private RevisionHigiene mapear(ResultSet rs) throws SQLException {
        RevisionHigiene r = new RevisionHigiene();
        r.setIdRevision(rs.getInt("id_revision"));
        Date f = rs.getDate("fecha");
        if (f != null) r.setFecha(f.toLocalDate());
        r.setArea(rs.getString("area"));
        r.setResponsable(rs.getString("responsable"));
        int emp = rs.getInt("fk_id_empleado");
        if (!rs.wasNull()) r.setFkIdEmpleado(emp);
        r.setPuntaje(rs.getBigDecimal("puntaje"));
        r.setEstado(rs.getString("estado"));
        r.setObservaciones(rs.getString("observaciones"));
        Timestamp fr = rs.getTimestamp("fecha_registro");
        if (fr != null) r.setFechaRegistro(fr.toLocalDateTime());
        return r;
    }
}
