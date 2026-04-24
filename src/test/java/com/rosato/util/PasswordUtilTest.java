package com.rosato.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    void hashYVerificarExitoso() {
        String h = PasswordUtil.hash("Admin123");
        assertNotEquals("Admin123", h);
        assertTrue(PasswordUtil.verificar("Admin123", h));
        assertFalse(PasswordUtil.verificar("otra", h));
    }

    @Test
    void verificarNullNoRompe() {
        assertFalse(PasswordUtil.verificar(null, null));
        assertFalse(PasswordUtil.verificar("x", null));
        assertFalse(PasswordUtil.verificar(null, "x"));
    }

    @Test
    void reglasDeFortaleza() {
        assertTrue(PasswordUtil.esFuerte("Admin123"));
        assertTrue(PasswordUtil.esFuerte("Clave2025"));
        assertFalse(PasswordUtil.esFuerte("cortita"));        // < 8
        assertFalse(PasswordUtil.esFuerte("sinmayusc1"));      // sin mayúscula
        assertFalse(PasswordUtil.esFuerte("SinNumero"));       // sin número
        assertFalse(PasswordUtil.esFuerte(null));
    }
}
