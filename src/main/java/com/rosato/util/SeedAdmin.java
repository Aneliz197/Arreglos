package com.rosato.util;

import com.rosato.dao.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Crea (o actualiza) el empleado administrador por defecto.
 *
 * Uso (una vez aplicado el esquema SQL):
 *   mvn -q compile exec:java -Dexec.mainClass="com.rosato.util.SeedAdmin"
 *       -Dexec.args="admin Admin123"
 */
public final class SeedAdmin {

    public static void main(String[] args) throws SQLException {
        String usuario = args.length > 0 ? args[0] : "admin";
        String pass    = args.length > 1 ? args[1] : "Admin123";

        if (!PasswordUtil.esFuerte(pass)) {
            System.err.println("La contraseña no cumple requisitos (8+ chars, 1 mayúscula, 1 número).");
            System.exit(2);
        }

        String hash = PasswordUtil.hash(pass);

        try (Connection c = ConexionBD.get()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id_empleado FROM Empleado WHERE usuario = ?")) {
                ps.setString(1, usuario);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement u = c.prepareStatement(
                                "UPDATE Empleado SET contrasena = ?, activo = TRUE, " +
                                "intentos_fallidos = 0, bloqueado_hasta = NULL WHERE id_empleado = ?")) {
                            u.setString(1, hash);
                            u.setInt(2, rs.getInt(1));
                            u.executeUpdate();
                        }
                        System.out.println("Administrador '" + usuario + "' actualizado.");
                        return;
                    }
                }
            }
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO Empleado (nombre_completo, cedula, telefono, usuario, " +
                    "contrasena, area_trabajo, activo) VALUES (?, ?, ?, ?, ?, ?, TRUE)")) {
                ins.setString(1, "Administrador Rosato");
                ins.setString(2, "000-0000000-0");
                ins.setString(3, "8090000000");
                ins.setString(4, usuario);
                ins.setString(5, hash);
                ins.setString(6, "Administrador");
                ins.executeUpdate();
            }
            System.out.println("Administrador '" + usuario + "' creado.");
        }
    }

    private SeedAdmin() { }
}
