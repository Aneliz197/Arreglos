package com.rosato.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class HigieneCalc {

    public enum Estado { OK, OBSERVADO, NO_CONFORME }

    public static BigDecimal porcentaje(int cumplidos, int total) {
        if (total <= 0) return BigDecimal.ZERO;
        return BigDecimal.valueOf(cumplidos)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    public static Estado evaluar(BigDecimal porcentaje) {
        if (porcentaje == null) return Estado.NO_CONFORME;
        if (porcentaje.compareTo(BigDecimal.valueOf(90)) >= 0) return Estado.OK;
        if (porcentaje.compareTo(BigDecimal.valueOf(70)) >= 0) return Estado.OBSERVADO;
        return Estado.NO_CONFORME;
    }

    public static String estadoLabel(Estado e) {
        return switch (e) {
            case OK -> "OK";
            case OBSERVADO -> "Observado";
            case NO_CONFORME -> "No conforme";
        };
    }

    private HigieneCalc() { }
}
