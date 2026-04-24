package com.rosato.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Pedido {
    private Integer idPedido;
    private Integer fkIdCliente;
    private Integer fkIdEmpleado;
    private LocalDateTime fechaPedido;
    private LocalDateTime fechaEntrega;
    private String estado = "Borrador";
    private String tipoEntrega = "Local";
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal adelanto = BigDecimal.ZERO;
    private BigDecimal saldoPendiente = BigDecimal.ZERO;
    private String observaciones;

    // Campos "derivados" para listados (opcional).
    private String nombreCliente;

    public Pedido() { }

    public Integer getIdPedido() { return idPedido; }
    public void setIdPedido(Integer idPedido) { this.idPedido = idPedido; }

    public Integer getFkIdCliente() { return fkIdCliente; }
    public void setFkIdCliente(Integer fkIdCliente) { this.fkIdCliente = fkIdCliente; }

    public Integer getFkIdEmpleado() { return fkIdEmpleado; }
    public void setFkIdEmpleado(Integer fkIdEmpleado) { this.fkIdEmpleado = fkIdEmpleado; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String tipoEntrega) { this.tipoEntrega = tipoEntrega; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getAdelanto() { return adelanto; }
    public void setAdelanto(BigDecimal adelanto) { this.adelanto = adelanto; }

    public BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(BigDecimal saldoPendiente) { this.saldoPendiente = saldoPendiente; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}
