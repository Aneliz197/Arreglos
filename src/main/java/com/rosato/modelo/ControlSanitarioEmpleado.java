package com.rosato.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ControlSanitarioEmpleado {
    private Integer idControl;
    private LocalDate fecha;
    private int fkIdEmpleado;
    private boolean uniformeOk;
    private boolean manosOk;
    private boolean redecillaOk;
    private boolean guantesOk;
    private boolean unasOk;
    private boolean saludOk;
    private String observaciones;
    private LocalDateTime fechaRegistro;

    private String nombreEmpleado;
    private String areaEmpleado;

    public Integer getIdControl() { return idControl; }
    public void setIdControl(Integer v) { this.idControl = v; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate v) { this.fecha = v; }
    public int getFkIdEmpleado() { return fkIdEmpleado; }
    public void setFkIdEmpleado(int v) { this.fkIdEmpleado = v; }
    public boolean isUniformeOk() { return uniformeOk; }
    public void setUniformeOk(boolean v) { this.uniformeOk = v; }
    public boolean isManosOk() { return manosOk; }
    public void setManosOk(boolean v) { this.manosOk = v; }
    public boolean isRedecillaOk() { return redecillaOk; }
    public void setRedecillaOk(boolean v) { this.redecillaOk = v; }
    public boolean isGuantesOk() { return guantesOk; }
    public void setGuantesOk(boolean v) { this.guantesOk = v; }
    public boolean isUnasOk() { return unasOk; }
    public void setUnasOk(boolean v) { this.unasOk = v; }
    public boolean isSaludOk() { return saludOk; }
    public void setSaludOk(boolean v) { this.saludOk = v; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String v) { this.observaciones = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }
    public String getNombreEmpleado() { return nombreEmpleado; }
    public void setNombreEmpleado(String v) { this.nombreEmpleado = v; }
    public String getAreaEmpleado() { return areaEmpleado; }
    public void setAreaEmpleado(String v) { this.areaEmpleado = v; }

    public int totalChecks() { return 6; }
    public int aprobadas() {
        int n = 0;
        if (uniformeOk)  n++;
        if (manosOk)     n++;
        if (redecillaOk) n++;
        if (guantesOk)   n++;
        if (unasOk)      n++;
        if (saludOk)     n++;
        return n;
    }
}
