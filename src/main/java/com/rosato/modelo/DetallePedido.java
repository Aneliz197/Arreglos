package com.rosato.modelo;

import java.math.BigDecimal;

public class DetallePedido {
    private Integer idDetalle;
    private Integer fkIdPedido;
    private String tipoProducto;
    private BigDecimal cantidadLibras;
    private String diseno;
    private boolean disenoComplejo;
    private String colaboradorExterno;
    private BigDecimal tiempoEstimadoHoras;
    private BigDecimal precioBase = BigDecimal.ZERO;
    private BigDecimal costoDiseno = BigDecimal.ZERO;
    private boolean contieneFresas;

    public DetallePedido() { }

    public Integer getIdDetalle() { return idDetalle; }
    public void setIdDetalle(Integer idDetalle) { this.idDetalle = idDetalle; }

    public Integer getFkIdPedido() { return fkIdPedido; }
    public void setFkIdPedido(Integer fkIdPedido) { this.fkIdPedido = fkIdPedido; }

    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String tipoProducto) { this.tipoProducto = tipoProducto; }

    public BigDecimal getCantidadLibras() { return cantidadLibras; }
    public void setCantidadLibras(BigDecimal cantidadLibras) { this.cantidadLibras = cantidadLibras; }

    public String getDiseno() { return diseno; }
    public void setDiseno(String diseno) { this.diseno = diseno; }

    public boolean isDisenoComplejo() { return disenoComplejo; }
    public void setDisenoComplejo(boolean disenoComplejo) { this.disenoComplejo = disenoComplejo; }

    public String getColaboradorExterno() { return colaboradorExterno; }
    public void setColaboradorExterno(String colaboradorExterno) { this.colaboradorExterno = colaboradorExterno; }

    public BigDecimal getTiempoEstimadoHoras() { return tiempoEstimadoHoras; }
    public void setTiempoEstimadoHoras(BigDecimal tiempoEstimadoHoras) { this.tiempoEstimadoHoras = tiempoEstimadoHoras; }

    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }

    public BigDecimal getCostoDiseno() { return costoDiseno; }
    public void setCostoDiseno(BigDecimal costoDiseno) { this.costoDiseno = costoDiseno; }

    public boolean isContieneFresas() { return contieneFresas; }
    public void setContieneFresas(boolean contieneFresas) { this.contieneFresas = contieneFresas; }
}
