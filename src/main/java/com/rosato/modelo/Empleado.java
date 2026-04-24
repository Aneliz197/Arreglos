package com.rosato.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Empleado {
    private Integer idEmpleado;
    private String nombreCompleto;
    private String cedula;
    private String telefono;
    private String usuario;
    private String contrasena;      // hash
    private String areaTrabajo;
    private String experiencia;
    private String disponibilidad;
    private Double salario;
    private LocalDate fechaContratacion;
    private LocalDate fechaNacimiento;
    private LocalDate fechaPruebaEmbarazo;
    private boolean activo = true;
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    public Empleado() { }

    public Integer getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Integer idEmpleado) { this.idEmpleado = idEmpleado; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getContrasena() { return contrasena; }
    public void setContrasena(String contrasena) { this.contrasena = contrasena; }

    public String getAreaTrabajo() { return areaTrabajo; }
    public void setAreaTrabajo(String areaTrabajo) { this.areaTrabajo = areaTrabajo; }

    public String getExperiencia() { return experiencia; }
    public void setExperiencia(String experiencia) { this.experiencia = experiencia; }

    public String getDisponibilidad() { return disponibilidad; }
    public void setDisponibilidad(String disponibilidad) { this.disponibilidad = disponibilidad; }

    public Double getSalario() { return salario; }
    public void setSalario(Double salario) { this.salario = salario; }

    public LocalDate getFechaContratacion() { return fechaContratacion; }
    public void setFechaContratacion(LocalDate fechaContratacion) { this.fechaContratacion = fechaContratacion; }

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public LocalDate getFechaPruebaEmbarazo() { return fechaPruebaEmbarazo; }
    public void setFechaPruebaEmbarazo(LocalDate fechaPruebaEmbarazo) { this.fechaPruebaEmbarazo = fechaPruebaEmbarazo; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public int getIntentosFallidos() { return intentosFallidos; }
    public void setIntentosFallidos(int intentosFallidos) { this.intentosFallidos = intentosFallidos; }

    public LocalDateTime getBloqueadoHasta() { return bloqueadoHasta; }
    public void setBloqueadoHasta(LocalDateTime bloqueadoHasta) { this.bloqueadoHasta = bloqueadoHasta; }
}
