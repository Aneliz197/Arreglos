package com.rosato.dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fábrica de conexiones JDBC. Lee la configuración desde
 * /com/rosato/config.properties y permite sobrescribir con
 * variables de entorno (ROSATO_DB_URL, ROSATO_DB_USER, ROSATO_DB_PASSWORD).
 */
public final class ConexionBD {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = ConexionBD.class
                .getResourceAsStream("/com/rosato/config.properties")) {
            if (in != null) PROPS.load(in);
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo cargar config.properties", ex);
        }
    }

    private ConexionBD() { }

    private static String prop(String key, String envVar, String fallback) {
        String v = System.getenv(envVar);
        if (v != null && !v.isBlank()) return v;
        return PROPS.getProperty(key, fallback);
    }

    public static Connection get() throws SQLException {
        String url  = prop("db.url",      "ROSATO_DB_URL",      null);
        String user = prop("db.user",     "ROSATO_DB_USER",     "root");
        String pass = prop("db.password", "ROSATO_DB_PASSWORD", "");
        if (url == null) {
            throw new SQLException("db.url no configurada (config.properties o ROSATO_DB_URL).");
        }
        return DriverManager.getConnection(url, user, pass);
    }

    public static int getIntProp(String key, int fallback) {
        try {
            return Integer.parseInt(PROPS.getProperty(key, String.valueOf(fallback)));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
