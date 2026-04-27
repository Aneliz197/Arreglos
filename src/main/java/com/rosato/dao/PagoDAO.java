package com.rosato.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class PagoDAO {

    public void registrar(Connection c, int idPedido, String tipoPago,
                          String metodoPago, BigDecimal monto,
                          String referencia, Integer idEmpleado) throws SQLException {
        String sql = """
            INSERT INTO Pago (fk_id_pedido, tipo_pago, metodo_pago, monto, referencia, recibido_por)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idPedido);
            ps.setString(2, tipoPago);
            ps.setString(3, metodoPago);
            ps.setBigDecimal(4, monto);
            if (referencia == null || referencia.isBlank()) ps.setNull(5, Types.VARCHAR);
            else ps.setString(5, referencia);
            if (idEmpleado == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, idEmpleado);
            ps.executeUpdate();
        }
    }
}
