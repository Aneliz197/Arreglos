package com.rosato.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Cálculos auxiliares del módulo de inventario. */
public final class InventarioCalc {

    private InventarioCalc() { }

    /**
     * Nuevo costo promedio ponderado al ingresar {@code cantidad} a
     * {@code precioUnitario}, dado el stock/costo previos.  Si el nuevo
     * stock queda en 0 se devuelve el precio unitario de la entrada.
     */
    public static BigDecimal nuevoCostoPromedio(BigDecimal stockPrev, BigDecimal costoPrev,
                                                BigDecimal cantidad, BigDecimal precioUnitario) {
        BigDecimal nuevoStock = stockPrev.add(cantidad);
        if (nuevoStock.signum() == 0) return precioUnitario;
        BigDecimal valorPrev  = stockPrev.multiply(costoPrev);
        BigDecimal valorNuevo = cantidad.multiply(precioUnitario);
        return valorPrev.add(valorNuevo).divide(nuevoStock, 4, RoundingMode.HALF_UP);
    }
}
