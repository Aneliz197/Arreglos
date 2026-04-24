package com.rosato.dao;

import com.rosato.modelo.Cliente;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** DAO básico de Cliente (Módulos 1 y 2). */
public class ClienteDAO {

    public Optional<Cliente> porUsuario(String usuario) throws SQLException {
        String sql = "SELECT * FROM Cliente WHERE usuario = ? AND activo = TRUE";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, usuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<Cliente> porId(int id) throws SQLException {
        String sql = "SELECT * FROM Cliente WHERE id_cliente = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
            }
        }
        return Optional.empty();
    }

    public boolean existeUsuario(String usuario) throws SQLException {
        return porUsuario(usuario).isPresent();
    }

    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM Cliente WHERE email = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int insertar(Cliente cl) throws SQLException {
        String sql = """
            INSERT INTO Cliente
              (nombre, apellido, direccion, telefono, email, rnc_cedula,
               usuario, contrasena, acepta_terminos, activo)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, TRUE)
            """;
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cl.getNombre());
            ps.setString(2, cl.getApellido());
            ps.setString(3, cl.getDireccion());
            ps.setString(4, cl.getTelefono());
            ps.setString(5, cl.getEmail());
            ps.setString(6, cl.getRncCedula());
            ps.setString(7, cl.getUsuario());
            ps.setString(8, cl.getContrasena());
            ps.setBoolean(9, cl.isAceptaTerminos());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) {
                    cl.setIdCliente(k.getInt(1));
                    return cl.getIdCliente();
                }
            }
        }
        throw new SQLException("No se pudo insertar cliente.");
    }

    public void registrarIntentoFallido(int id) throws SQLException {
        String sql = "UPDATE Cliente SET intentos_fallidos = intentos_fallidos + 1 WHERE id_cliente = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void bloquearHasta(int id, LocalDateTime hasta) throws SQLException {
        String sql = "UPDATE Cliente SET bloqueado_hasta = ? WHERE id_cliente = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(hasta));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void reiniciarIntentos(int id) throws SQLException {
        String sql = "UPDATE Cliente SET intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id_cliente = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Cliente> listar(String filtro) throws SQLException {
        String sql = """
            SELECT * FROM Cliente
            WHERE (? = '' OR nombre LIKE ? OR apellido LIKE ? OR telefono LIKE ? OR rnc_cedula LIKE ?)
            ORDER BY fecha_registro DESC
            """;
        List<Cliente> out = new ArrayList<>();
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String f = filtro == null ? "" : filtro.trim();
            String like = "%" + f + "%";
            ps.setString(1, f);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            ps.setString(5, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(mapear(rs));
            }
        }
        return out;
    }

    public int contarPedidos(int idCliente) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Pedido WHERE fk_id_cliente = ?";
        try (Connection c = ConexionBD.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setDireccion(rs.getString("direccion"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setRncCedula(rs.getString("rnc_cedula"));
        c.setUsuario(rs.getString("usuario"));
        c.setContrasena(rs.getString("contrasena"));
        c.setAceptaTerminos(rs.getBoolean("acepta_terminos"));
        c.setActivo(rs.getBoolean("activo"));
        Timestamp fr = rs.getTimestamp("fecha_registro");
        if (fr != null) c.setFechaRegistro(fr.toLocalDateTime());
        c.setIntentosFallidos(rs.getInt("intentos_fallidos"));
        Timestamp bh = rs.getTimestamp("bloqueado_hasta");
        if (bh != null) c.setBloqueadoHasta(bh.toLocalDateTime());
        return c;
    }
}
