package com.rosato.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Mantenimiento {
    private Integer idMantenimiento;
    private int fkIdMaquina;
    private String tipo;          // Preventivo / Correctivo
    private LocalDate fecha;
    private String tecnico;
    private BigDecimal costo = BigDecimal.ZERO;
    private String descripcion;
    private LocalDate proximaRevision;
    private Integer fkIdEmpleado;
    private LocalDateTime fechaRegistro;

    // Derivados
    private String codigoMaquina;
    private String nombreMaquina;
    private String nombreEmpleado;

    public Integer getIdMantenimiento() { return idMantenimiento; }
    public void setIdMantenimiento(Integer v) { this.idMantenimiento = v; }
    public int getFkIdMaquina() { return fkIdMaquina; }
    public void setFkIdMaquina(int v) { this.fkIdMaquina = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }
    public String getTecnico() { return tecnico; }
    public void setTecnico(String v) { this.tecnico = v; }
    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal v) { this.costo = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public LocalDate getProximaRevision() { return proximaRevision; }
    public void setProximaRevision(LocalDate v) { this.proximaRevision = v; }
    public Integer getFkIdEmpleado() { return fkIdEmpleado; }
    public void setFkIdEmpleado(Integer v) { this.fkIdEmpleado = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }
    public String getCodigoMaquina() { return codigoMaquina; }
    public void setCodigoMaquina(String v) { this.codigoMaquina = v; }
    public String getNombreMaquina() { return nombreMaquina; }
    public void setNombreMaquina(String v) { this.nombreMaquina = v; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String v) { this.nombreEmpleado = v; }
}
