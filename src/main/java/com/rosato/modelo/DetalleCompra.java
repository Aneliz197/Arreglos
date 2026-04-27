package com.rosato.modelo;

import java.math.BigDecimal;

public class DetalleCompra {
    private Integer idDetalle;
    private int fkIdCompra;
    private int fkIdIngrediente;
    private BigDecimal cantidad = BigDecimal.ZERO;
    private BigDecimal precioUnitario = BigDecimal.ZERO;
    private BigDecimal subtotal = BigDecimal.ZERO;
    // Derivados para UI
    private String nombreIngrediente;
    private String unidad;

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer v) { this.idDetalle = v; }
    public int getFkIdCompra() { return fkIdCompra; }
    public void setFkIdCompra(int v) { this.fkIdCompra = v; }
    public int getFkIdIngrediente() { return fkIdIngrediente; }
    public void setFkIdIngrediente(int v) { this.fkIdIngrediente = v; }
    public BigDecimal getCantidad() { return cantidad; }
    public void setCantidad(BigDecimal v) { this.cantidad = v; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal v) { this.precioUnitario = v; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal v) { this.subtotal = v; }
    public String getNombreIngrediente() { return nombreIngrediente; }
    public void setNombreIngrediente(String v) { this.nombreIngrediente = v; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String v) { this.unidad = v; }
}
