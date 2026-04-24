package com.rosato.util;

import com.rosato.modelo.DetallePedido;
import com.rosato.modelo.Ingrediente;
import com.rosato.modelo.Receta;
import com.rosato.modelo.RecetaIngrediente;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio puro que calcula los requerimientos de ingredientes para un
 * conjunto de detalles de pedido dadas las recetas (una por
 * tipo_producto) y el stock actual de ingredientes.
 */
public final class PlanProduccionService {

    private PlanProduccionService() { }

    public static class Requerimiento {
        public final int idIngrediente;
        public final String nombre;
        public final String unidad;
        public final BigDecimal requerido;
        public final BigDecimal disponible;

        public Requerimiento(int id, String nombre, String unidad,
                             BigDecimal requerido, BigDecimal disponible) {
            this.idIngrediente = id;
            this.nombre = nombre;
            this.unidad = unidad;
            this.requerido = requerido;
            this.disponible = disponible;
        }

        public BigDecimal getFaltante() {
            BigDecimal f = requerido.subtract(disponible);
            return f.signum() > 0 ? f : BigDecimal.ZERO;
        }
        public boolean isFaltante() { return getFaltante().signum() > 0; }
        public String estado() { return isFaltante() ? "⚠ Faltan " + getFaltante().toPlainString() + " " + unidad : "OK"; }

        public int getIdIngrediente() { return idIngrediente; }
        public String getNombre()     { return nombre; }
        public String getUnidad()     { return unidad; }
        public BigDecimal getRequerido()  { return requerido; }
        public BigDecimal getDisponible() { return disponible; }
    }

    /**
     * Dados varios detalles de pedido, sus recetas por tipo y el stock
     * actual de ingredientes, devuelve el listado consolidado de
     * requerimientos con faltantes.
     */
    public static List<Requerimiento> calcularRequerimientos(
            List<DetallePedido> detalles,
            Map<String, Receta> recetasPorTipo,
            Map<Integer, Ingrediente> ingredientesPorId) {

        Map<Integer, BigDecimal> requeridoPorIng = new LinkedHashMap<>();

        for (DetallePedido d : detalles) {
            Receta r = recetasPorTipo.get(d.getTipoProducto());
            if (r == null) continue; // sin receta -> no se puede estimar
            BigDecimal libras = d.getCantidadLibras() == null ? BigDecimal.ZERO : d.getCantidadLibras();
            for (RecetaIngrediente ri : r.getIngredientes()) {
                BigDecimal aportado = ri.getCantidadPorLibra().multiply(libras);
                requeridoPorIng.merge(ri.getFkIdIngrediente(), aportado, BigDecimal::add);
            }
        }

        List<Requerimiento> out = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> e : requeridoPorIng.entrySet()) {
            Ingrediente ing = ingredientesPorId.get(e.getKey());
            BigDecimal disponible = ing == null || ing.getStockActual() == null
                    ? BigDecimal.ZERO : ing.getStockActual();
            String nombre = ing == null ? ("#" + e.getKey()) : ing.getNombre();
            String unidad = ing == null ? "" : ing.getUnidad();
            out.add(new Requerimiento(e.getKey(), nombre, unidad,
                    e.getValue().setScale(3, RoundingMode.HALF_UP), disponible));
        }
        return out;
    }
}
