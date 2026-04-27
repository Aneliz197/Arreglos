package com.rosato.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventarioCalcTest {

    @Test
    void primeraEntradaFijaElCosto() {
        BigDecimal nuevo = InventarioCalc.nuevoCostoPromedio(
                bd("0"), bd("0"), bd("10"), bd("25"));
        assertEquals(bd("25.0000"), nuevo);
    }

    @Test
    void promedioPonderadoConStockPrevio() {
        // 10 @ 20 + 10 @ 30 = 25
        BigDecimal nuevo = InventarioCalc.nuevoCostoPromedio(
                bd("10"), bd("20"), bd("10"), bd("30"));
        assertEquals(bd("25.0000"), nuevo);
    }

    @Test
    void ingresoMasBaratoBajaElPromedio() {
        // 5 @ 40 + 5 @ 20 = 30
        BigDecimal nuevo = InventarioCalc.nuevoCostoPromedio(
                bd("5"), bd("40"), bd("5"), bd("20"));
        assertEquals(bd("30.0000"), nuevo);
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }
}
