package com.rosato.modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Maquina {
    private Integer idMaquina;
    private String codigo;
    private String nombre;
    private String tipo;
    private String marca;
    private String modelo;
    private String serial;
    private LocalDate fechaAdquisicion;
    private String estado = "Operativa";
    private LocalDate proximaRevision;
    private String ubicacion;
    private String notas;
    private boolean activo = true;
    private LocalDateTime fechaRegistro;

    public Integer getIdMaquina() { return idMaquina; }
    public void setIdMaquina(Integer v) { this.idMaquina = v; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String v) { this.codigo = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getTipo() { return tipo; }
    public void setTipo(String v) { this.tipo = v; }
    public String getMarca() { return marca; }
    public void setMarca(String v) { this.marca = v; }
    public String getModelo() { return modelo; }
    public void setModelo(String v) { this.modelo = v; }
    public String getSerial() { return serial; }
    public void setSerial(String v) { this.serial = v; }
    public LocalDate getFechaAdquisicion() { return fechaAdquisicion; }
    public void setFechaAdquisicion(LocalDate v) { this.fechaAdquisicion = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { this.estado = v; }
    public LocalDate getProximaRevision() { return proximaRevision; }
    public void setProximaRevision(LocalDate v) { this.proximaRevision = v; }
    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String v) { this.ubicacion = v; }
    public String getNotas() { return notas; }
    public void setNotas(String v) { this.notas = v; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean v) { this.activo = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }

    @Override public String toString() {
        return codigo + " · " + nombre;
    }
}
