package com.rosato.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RevisionHigiene {
    private Integer idRevision;
    private LocalDate fecha;
    private String area;
    private String responsable;
    private Integer fkIdEmpleado;
    private BigDecimal puntaje = BigDecimal.ZERO;
    private String estado = "Observado";
    private String observaciones;
    private LocalDateTime fechaRegistro;
    private List<RevisionHigieneItem> items = new ArrayList<>();

    public Integer getIdRevision() { return idRevision; }
    public void setIdRevision(Integer v) { this.idRevision = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }
    public String getArea() { return area; }
    public void setArea(String v) { this.area = v; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String v) { this.responsable = v; }
    public Integer getFkIdEmpleado() { return fkIdEmpleado; }
    public void setFkIdEmpleado(Integer v) { this.fkIdEmpleado = v; }
    public BigDecimal getPuntaje() { return puntaje; }
    public void setPuntaje(BigDecimal v) { this.puntaje = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { this.observaciones = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }
    public List<RevisionHigieneItem> getItems() { return items; }
    public void setItems(List<RevisionHigieneItem> v) { this.items = v; }
}
