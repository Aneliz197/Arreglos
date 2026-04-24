package com.rosato.modelo;

import java.math.BigDecimal;

public class RecetaIngrediente {
    private Integer idRecetaIng;
    private int fkIdReceta;
    private int fkIdIngrediente;
    private BigDecimal cantidadPorLibra = BigDecimal.ZERO;
    // Derivados
    private String nombreIngrediente;
    private String unidad;

    public Integer getIdRecetaIng() { return idRecetaIng; }
    public void setIdRecetaIng(Integer v) { this.idRecetaIng = v; }
    public int getFkIdReceta() { return fkIdReceta; }
    public void setFkIdReceta(int v) { this.fkIdReceta = v; }
    public int getFkIdIngrediente() { return fkIdIngrediente; }
    public void setFkIdIngrediente(int v) { this.fkIdIngrediente = v; }
    public BigDecimal getCantidadPorLibra() { return cantidadPorLibra; }
    public void setCantidadPorLibra(BigDecimal v) { this.cantidadPorLibra = v; }
    public String getNombreIngrediente() { return nombreIngrediente; }
    public void setNombreIngrediente(String v) { this.nombreIngrediente = v; }
    public String getUnidad() { return unidad; }
    public void setUnidad(String v) { this.unidad = v; }
}
