package com.rosato.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.rosato.util.HigieneCalc.Estado.*;
import static org.junit.jupiter.api.Assertions.*;

class HigieneCalcTest {

    @Test
    void porcentajeBase() {
        assertEquals(0, new BigDecimal("100.00").compareTo(HigieneCalc.porcentaje(6, 6)));
        assertEquals(0, new BigDecimal("83.33") .compareTo(HigieneCalc.porcentaje(5, 6)));
        assertEquals(0, BigDecimal.ZERO         .compareTo(HigieneCalc.porcentaje(0, 6)));
        assertEquals(0, BigDecimal.ZERO         .compareTo(HigieneCalc.porcentaje(3, 0)));
    }

    @Test
    void estadoSegunPorcentaje() {
        assertEquals(OK,          HigieneCalc.evaluar(new BigDecimal("90.00")));
        assertEquals(OK,          HigieneCalc.evaluar(new BigDecimal("100.00")));
        assertEquals(OBSERVADO,   HigieneCalc.evaluar(new BigDecimal("83.33")));
        assertEquals(OBSERVADO,   HigieneCalc.evaluar(new BigDecimal("70.00")));
        assertEquals(NO_CONFORME, HigieneCalc.evaluar(new BigDecimal("69.99")));
        assertEquals(NO_CONFORME, HigieneCalc.evaluar(null));
    }
}
