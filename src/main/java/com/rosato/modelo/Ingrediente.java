package com.rosato.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ingrediente {
    private Integer idIngrediente;
    private String nombre;
    private String unidad;
    private BigDecimal stockActual = BigDecimal.ZERO;
    private BigDecimal stockMinimo = BigDecimal.ZERO;
    private BigDecimal costoPromedio = BigDecimal.ZERO;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

    public Integer getIdIngrediente() { return idIngrediente; }
    public void setIdIngrediente(Integer v) { this.idIngrediente = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String v) { this.unidad = v; }
    public BigDecimal getStockActual() { return stockActual; }
    public void setStockActual(BigDecimal v) { this.stockActual = v; }
    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal v) { this.stockMinimo = v; }
    public BigDecimal getCostoPromedio() { return costoPromedio; }
    public void setCostoPromedio(BigDecimal v) { this.costoPromedio = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }

    public boolean isBajoStock() {
        return stockActual != null && stockMinimo != null
                && stockActual.compareTo(stockMinimo) < 0;
    }
}
