package com.rosato.modelo;

import java.time.LocalDateTime;

public class Proveedor {
    private Integer idProveedor;
    private String nombre;
    private String rnc;
    private String telefono;
    private String email;
    private String direccion;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

    public Integer getIdProveedor() { return idProveedor; }
    public void setIdProveedor(Integer v) { this.idProveedor = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getRnc() { return rnc; }
    public void setRnc(String v) { this.rnc = v; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String v) { this.telefono = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String v) { this.direccion = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }

    @Override public String toString() {
        return (idProveedor == null ? "" : "#" + idProveedor + " · ") + nombre;
    }
}
