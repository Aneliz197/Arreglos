package com.rosato.modelo;

import java.math.BigDecimal;

public class CapacitacionEmpleado {
    private Integer idCapacitacionEmp;
    private int fkIdCapacitacion;
    private int fkIdEmpleado;
    private boolean asistio;
    private BigDecimal calificacion;
    private String observaciones;

    // Derivado
    private String nombreEmpleado;
    private String areaEmpleado;

    public Integer getIdCapacitacionEmp() { return idCapacitacionEmp; }
    public void setIdCapacitacionEmp(Integer v) { this.idCapacitacionEmp = v; }
    public int getFkIdCapacitacion() { return fkIdCapacitacion; }
    public void setFkIdCapacitacion(int v) { this.fkIdCapacitacion = v; }
    public int getFkIdEmpleado() { return fkIdEmpleado; }
    public void setFkIdEmpleado(int v) { this.fkIdEmpleado = v; }
    public boolean isAsistio() { return asistio; }
    public void setAsistio(boolean v) { this.asistio = v; }
    public BigDecimal getCalificacion() { return calificacion; }
    public void setCalificacion(BigDecimal v) { this.calificacion = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { this.observaciones = v; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String v) { this.nombreEmpleado = v; }
    public String getAreaEmpleado() { return areaEmpleado; }
    public void setAreaEmpleado(String v) { this.areaEmpleado = v; }
}
