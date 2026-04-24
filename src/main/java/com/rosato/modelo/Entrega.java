package com.rosato.modelo;

import java.time.LocalDateTime;

public class Entrega {
    private Integer idEntrega;
    private int fkIdPedido;
    private String tipoEntrega;        // 'Local' / 'Domicilio'
    private String direccion;
    private LocalDateTime fechaProgramada;
    private LocalDateTime fechaReal;
    private String estado = "Pendiente"; // Pendiente, En ruta, Entregada, Fallida
    private Integer fkIdRepartidor;
    private String notas;

    // Derivados
    private String nombreCliente;
    private String nombreRepartidor;
    private java.math.BigDecimal saldoPendiente;
    private String estadoPedido;

    public Integer getIdEntrega() { return idEntrega; }
    public void setIdEntrega(Integer v) { this.idEntrega = v; }
    public int getFkIdPedido() { return fkIdPedido; }
    public void setFkIdPedido(int v) { this.fkIdPedido = v; }
    public String getTipoEntrega() { return tipoEntrega; }
    public void setTipoEntrega(String v) { this.tipoEntrega = v; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    public void setFechaProgramada(LocalDateTime v) { this.fechaProgramada = v; }
    public LocalDateTime getFechaReal() { return fechaReal; }
    public void setFechaReal(LocalDateTime v) { this.fechaReal = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public Integer getFkIdRepartidor() { return fkIdRepartidor; }
    public void setFkIdRepartidor(Integer v) { this.fkIdRepartidor = v; }
    public String getNotas() { return notas; }
    public void setNotas(String v) { this.notas = v; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String v) { this.nombreCliente = v; }
    public String getNombreRepartidor() { return nombreRepartidor; }
    public void setNombreRepartidor(String v) { this.nombreRepartidor = v; }
    public java.math.BigDecimal getSaldoPendiente() { return saldoPendiente; }
    public void setSaldoPendiente(java.math.BigDecimal v) { this.saldoPendiente = v; }
    public String getEstadoPedido() { return estadoPedido; }
    public void setEstadoPedido(String v) { this.estadoPedido = v; }
}
