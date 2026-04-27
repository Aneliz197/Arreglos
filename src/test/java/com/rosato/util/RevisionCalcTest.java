package com.rosato.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static com.rosato.util.RevisionCalc.Estado.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RevisionCalcTest {

    private final LocalDate hoy = LocalDate.of(2025, 6, 10);

    @Test
    void sinFechaEsOk() {
        assertEquals(OK, RevisionCalc.evaluar(null, hoy, 7));
    }

    @Test
    void revisionPasadaEsVencida() {
        assertEquals(VENCIDA, RevisionCalc.evaluar(hoy.minusDays(1), hoy, 7));
    }

    @Test
    void revisionDentroDelAvisoEsProxima() {
        assertEquals(PROXIMA, RevisionCalc.evaluar(hoy.plusDays(3), hoy, 7));
        assertEquals(PROXIMA, RevisionCalc.evaluar(hoy.plusDays(7), hoy, 7));
    }

    @Test
    void revisionLejanaEsOk() {
        assertEquals(OK, RevisionCalc.evaluar(hoy.plusDays(30), hoy, 7));
    }
}
