package com.rosato.dao;

import com.rosato.modelo.DetallePedido;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetallePedidoDAO {

    public int insertar(Connection c, DetallePedido d) throws SQLException {
        String sql = """
            INSERT INTO DetallePedido
              (fk_id_pedido, tipo_producto, cantidad_libras, diseno,
               diseno_complejo, colaborador_externo, tiempo_estimado_horas,
               precio_base, costo_diseno, contiene_fresas)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, d.getFkIdPedido());
            ps.setString(2, d.getTipoProducto());
            ps.setBigDecimal(3, d.getCantidadLibras());
            ps.setString(4, d.getDiseno());
            ps.setBoolean(5, d.isDisenoComplejo());
            ps.setString(6, d.getColaboradorExterno());
            ps.setBigDecimal(7, d.getTiempoEstimadoHoras());
            ps.setBigDecimal(8, d.getPrecioBase());
            ps.setBigDecimal(9, d.getCostoDiseno());
            ps.setBoolean(10, d.isContieneFresas());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) {
                    d.setIdDetalle(k.getInt(1));
                    return d.getIdDetalle();
                }
            }
        }
        throw new SQLException("No se pudo insertar detalle de pedido.");
    }

    public List<DetallePedido> porPedido(int idPedido) throws SQLException {
        String sql = "SELECT * FROM DetallePedido WHERE fk_id_pedido = ?";
        List<DetallePedido> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    private DetallePedido mapear(ResultSet rs) throws SQLException {
        DetallePedido d = new DetallePedido();
        d.setIdDetalle(rs.getInt("id_detalle"));
        d.setFkIdPedido(rs.getInt("fk_id_pedido"));
        d.setTipoProducto(rs.getString("tipo_producto"));
        d.setCantidadLibras(rs.getBigDecimal("cantidad_libras"));
        d.setDiseno(rs.getString("diseno"));
        d.setDisenoComplejo(rs.getBoolean("diseno_complejo"));
        d.setColaboradorExterno(rs.getString("colaborador_externo"));
        d.setTiempoEstimadoHoras(rs.getBigDecimal("tiempo_estimado_horas"));
        d.setPrecioBase(rs.getBigDecimal("precio_base"));
        d.setCostoDiseno(rs.getBigDecimal("costo_diseno"));
        d.setContieneFresas(rs.getBoolean("contiene_fresas"));
        return d;
    }
}
