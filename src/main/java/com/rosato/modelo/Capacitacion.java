package com.rosato.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Capacitacion {
    private Integer idCapacitacion;
    private String titulo;
    private LocalDate fecha;
    private String instructor;
    private BigDecimal horas = BigDecimal.ZERO;
    private String ubicacion;
    private String descripcion;
    private LocalDateTime fechaRegistro;

    public Integer getIdCapacitacion() { return idCapacitacion; }
    public void setIdCapacitacion(Integer v) { this.idCapacitacion = v; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String v) { this.titulo = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }
    public String getInstructor() { return instructor; }
    public void setInstructor(String v) { this.instructor = v; }
    public BigDecimal getHoras() { return horas; }
    public void setHoras(BigDecimal v) { this.horas = v; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String v) { this.ubicacion = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }

    @Override public String toString() {
        return (fecha == null ? "" : fecha.toString()) + " · " + titulo;
    }
}
