package com.rosato.util;

import java.time.LocalDate;

/** Reglas simples de alerta para la próxima revisión de máquinas. */
public final class RevisionCalc {

    public enum Estado { OK, PROXIMA, VENCIDA }

    /** Marca como PROXIMA si faltan ≤ {@code diasAviso} días; VENCIDA si ya pasó. */
    public static Estado evaluar(LocalDate proximaRevision, LocalDate hoy, int diasAviso) {
        if (proximaRevision == null) return Estado.OK;
        if (proximaRevision.isBefore(hoy)) return Estado.VENCIDA;
        if (!proximaRevision.isAfter(hoy.plusDays(diasAviso))) return Estado.PROXIMA;
        return Estado.OK;
    }

    private RevisionCalc() { }
}
