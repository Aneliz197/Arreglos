package com.rosato.util;

import org.mindrot.jbcrypt.BCrypt;

import java.util.regex.Pattern;

/**
 * Utilidades para hash y verificación de contraseñas (BCrypt).
 */
public final class PasswordUtil {

    // Mínimo 8 chars, al menos 1 mayúscula y 1 número.
    private static final Pattern REGLAS =
            Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    private PasswordUtil() { }

    public static String hash(String plano) {
        return BCrypt.hashpw(plano, BCrypt.gensalt(10));
    }

    public static boolean verificar(String plano, String hash) {
        if (plano == null || hash == null || hash.isBlank()) return false;
        try {
            return BCrypt.checkpw(plano, hash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    /** Cumple los requisitos de fortaleza definidos en el Módulo 1. */
    public static boolean esFuerte(String plano) {
        return plano != null && REGLAS.matcher(plano).matches();
    }
}
