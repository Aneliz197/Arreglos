package com.rosato.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reglas de precio y tiempo estimado para pedidos.
 *
 * Precios base (RD$ por libra) configurables por tipo de producto.
 * Tiempo estimado: 0.5 lb = 3h, 6 lb = 8h, intermedio proporcional
 * (interpolación lineal).
 * Los diseños complejos suman un 25% del precio base.
 */
public final class PrecioService {

    /** Catálogo de productos y su precio por libra. */
    private static final Map<String, BigDecimal> PRECIO_POR_LB = new LinkedHashMap<>();

    static {
        PRECIO_POR_LB.put("Bizcocho",        new BigDecimal("450"));
        PRECIO_POR_LB.put("Cheesecake",      new BigDecimal("650"));
        PRECIO_POR_LB.put("Postre de copa",  new BigDecimal("500"));
        PRECIO_POR_LB.put("Galletas",        new BigDecimal("350"));
        PRECIO_POR_LB.put("Tres leches",     new BigDecimal("600"));
    }

    public static final BigDecimal LB_MIN = new BigDecimal("0.5");
    public static final BigDecimal LB_MAX = new BigDecimal("20");
    public static final BigDecimal FACTOR_DISENO_COMPLEJO = new BigDecimal("0.25");

    private PrecioService() { }

    public static String[] tiposProducto() {
        return PRECIO_POR_LB.keySet().toArray(new String[0]);
    }

    public static BigDecimal precioBase(String tipo, BigDecimal libras) {
        BigDecimal precioLb = PRECIO_POR_LB.getOrDefault(tipo, BigDecimal.ZERO);
        BigDecimal lb = libras == null ? BigDecimal.ZERO : libras;
        return precioLb.multiply(lb).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal costoDiseno(BigDecimal precioBase, boolean complejo) {
        if (!complejo || precioBase == null) return BigDecimal.ZERO;
        return precioBase.multiply(FACTOR_DISENO_COMPLEJO).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtotal(BigDecimal precioBase, BigDecimal costoDiseno) {
        return precioBase.add(costoDiseno).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal adelanto50(BigDecimal subtotal) {
        return subtotal.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal saldoPendiente(BigDecimal subtotal, BigDecimal adelanto) {
        return subtotal.subtract(adelanto).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Tiempo estimado en horas. Interpolación lineal entre
     * (0.5 lb, 3h) y (6 lb, 8h). Fuera del rango se extiende linealmente.
     */
    public static BigDecimal tiempoEstimadoHoras(BigDecimal libras) {
        if (libras == null) return BigDecimal.ZERO;
        double lb = libras.doubleValue();
        double horas = 3.0 + (lb - 0.5) * (5.0 / 5.5);
        if (horas < 1.0) horas = 1.0;
        return BigDecimal.valueOf(horas).setScale(2, RoundingMode.HALF_UP);
    }

    public static boolean librasValidas(BigDecimal lb) {
        return lb != null && lb.compareTo(LB_MIN) >= 0 && lb.compareTo(LB_MAX) <= 0;
    }
}
