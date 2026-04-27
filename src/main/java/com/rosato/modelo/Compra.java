package com.rosato.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Compra {
    private Integer idCompra;
    private int fkIdProveedor;
    private Integer fkIdEmpleado;
    private LocalDateTime fechaCompra;
    private String numeroFactura;
    private BigDecimal total = BigDecimal.ZERO;
    private String observaciones;
    // Derivado (para el listado)
    private String nombreProveedor;

    public Integer getIdCompra() { return idCompra; }
    public void setIdCompra(Integer v) { this.idCompra = v; }
    public int getFkIdProveedor() { return fkIdProveedor; }
    public void setFkIdProveedor(int v) { this.fkIdProveedor = v; }
    public Integer getFkIdEmpleado() { return fkIdEmpleado; }
    public void setFkIdEmpleado(Integer v) { this.fkIdEmpleado = v; }
    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public void setFechaCompra(LocalDateTime v) { this.fechaCompra = v; }
    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String v) { this.numeroFactura = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { this.total = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { this.observaciones = v; }
    public String getNombreProveedor() { return nombreProveedor; }
    public void setNombreProveedor(String v) { this.nombreProveedor = v; }
}
