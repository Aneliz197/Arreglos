package com.rosato.util;

import com.rosato.modelo.DetallePedido;
import com.rosato.modelo.Ingrediente;
import com.rosato.modelo.Receta;
import com.rosato.modelo.RecetaIngrediente;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PlanProduccionServiceTest {

    @Test
    void agregaRequerimientosAcumuladosPorIngrediente() {
        Receta bizcocho = recetaConIngredientes(1, "Bizcocho", Map.of(
                10, "0.5",   // harina 0.5 lb por libra
                20, "0.25")); // azúcar 0.25 lb por libra
        Receta galletas = recetaConIngredientes(2, "Galletas", Map.of(
                10, "0.3"));

        DetallePedido d1 = detalle("Bizcocho", "2");   // 2 lb
        DetallePedido d2 = detalle("Galletas", "1.5"); // 1.5 lb

        Map<String, Receta> recetasPorTipo = Map.of("Bizcocho", bizcocho, "Galletas", galletas);
        Map<Integer, Ingrediente> ings = new HashMap<>();
        ings.put(10, ing(10, "Harina", "lb", "2"));
        ings.put(20, ing(20, "Azúcar", "lb", "10"));

        List<PlanProduccionService.Requerimiento> req =
                PlanProduccionService.calcularRequerimientos(List.of(d1, d2), recetasPorTipo, ings);

        // Harina: 0.5*2 + 0.3*1.5 = 1 + 0.45 = 1.45 lb (disp 2) → OK
        var harina = req.stream().filter(r -> r.idIngrediente == 10).findFirst().orElseThrow();
        assertEquals(new BigDecimal("1.450"), harina.getRequerido());
        assertFalse(harina.isFaltante());

        // Azúcar: 0.25*2 = 0.5 lb (disp 10) → OK
        var azucar = req.stream().filter(r -> r.idIngrediente == 20).findFirst().orElseThrow();
        assertEquals(new BigDecimal("0.500"), azucar.getRequerido());
        assertFalse(azucar.isFaltante());
    }

    @Test
    void detectaFaltantesCuandoStockNoAlcanza() {
        Receta r = recetaConIngredientes(1, "Bizcocho", Map.of(10, "1"));
        DetallePedido d = detalle("Bizcocho", "5"); // requiere 5
        Map<Integer, Ingrediente> ings = Map.of(10, ing(10, "Harina", "lb", "2"));

        var req = PlanProduccionService.calcularRequerimientos(
                List.of(d), Map.of("Bizcocho", r), ings);
        assertEquals(1, req.size());
        var harina = req.get(0);
        assertTrue(harina.isFaltante());
        assertEquals(new BigDecimal("3.000"), harina.getFaltante());
    }

    @Test
    void ignoraDetallesSinReceta() {
        var req = PlanProduccionService.calcularRequerimientos(
                List.of(detalle("Cheesecake", "3")),
                Map.of(), Map.of());
        assertTrue(req.isEmpty());
    }

    private static Receta recetaConIngredientes(int id, String tipo, Map<Integer, String> cant) {
        Receta r = new Receta();
        r.setIdReceta(id);
        r.setTipoProducto(tipo);
        r.setNombre("Receta " + tipo);
        cant.forEach((idIng, c) -> {
            RecetaIngrediente ri = new RecetaIngrediente();
            ri.setFkIdReceta(id);
            ri.setFkIdIngrediente(idIng);
            ri.setCantidadPorLibra(new BigDecimal(c));
            r.getIngredientes().add(ri);
        });
        return r;
    }

    private static DetallePedido detalle(String tipo, String libras) {
        DetallePedido d = new DetallePedido();
        d.setTipoProducto(tipo);
        d.setCantidadLibras(new BigDecimal(libras));
        return d;
    }

    private static Ingrediente ing(int id, String nombre, String unidad, String stock) {
        Ingrediente i = new Ingrediente();
        i.setIdIngrediente(id);
        i.setNombre(nombre);
        i.setUnidad(unidad);
        i.setStockActual(new BigDecimal(stock));
        return i;
    }
}
