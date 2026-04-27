package com.rosato.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PrecioServiceTest {

    @Test
    void precioBaseBizcocho() {
        BigDecimal pb = PrecioService.precioBase("Bizcocho", new BigDecimal("2"));
        assertEquals(0, pb.compareTo(new BigDecimal("900.00")));
    }

    @Test
    void disenoComplejoSuma25PorCiento() {
        BigDecimal pb = new BigDecimal("1000");
        assertEquals(0, PrecioService.costoDiseno(pb, true).compareTo(new BigDecimal("250.00")));
        assertEquals(0, PrecioService.costoDiseno(pb, false).compareTo(BigDecimal.ZERO));
    }

    @Test
    void adelantoEsMitadDelSubtotal() {
        BigDecimal sub = new BigDecimal("1250.00");
        assertEquals(0, PrecioService.adelanto50(sub).compareTo(new BigDecimal("625.00")));
        assertEquals(0, PrecioService.saldoPendiente(sub, PrecioService.adelanto50(sub))
                .compareTo(new BigDecimal("625.00")));
    }

    @Test
    void librasValidas() {
        assertTrue(PrecioService.librasValidas(new BigDecimal("0.5")));
        assertTrue(PrecioService.librasValidas(new BigDecimal("20")));
        assertFalse(PrecioService.librasValidas(new BigDecimal("0.4")));
        assertFalse(PrecioService.librasValidas(new BigDecimal("20.01")));
        assertFalse(PrecioService.librasValidas(null));
    }

    @Test
    void tiempoEstimadoInterpolado() {
        // 0.5 lb -> 3h exactos
        assertEquals(0, PrecioService.tiempoEstimadoHoras(new BigDecimal("0.5"))
                .compareTo(new BigDecimal("3.00")));
        // 6 lb -> 8h exactos
        assertEquals(0, PrecioService.tiempoEstimadoHoras(new BigDecimal("6"))
                .compareTo(new BigDecimal("8.00")));
        // Intermedio monotónico creciente
        BigDecimal t1 = PrecioService.tiempoEstimadoHoras(new BigDecimal("1"));
        BigDecimal t2 = PrecioService.tiempoEstimadoHoras(new BigDecimal("3"));
        assertTrue(t2.compareTo(t1) > 0);
    }
}
