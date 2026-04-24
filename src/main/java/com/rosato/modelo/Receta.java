package com.rosato.modelo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Receta {
    private Integer idReceta;
    private String tipoProducto;
    private String nombre;
    private String notas;
    private boolean activa = true;
    private LocalDateTime fechaRegistro;
    private final List<RecetaIngrediente> ingredientes = new ArrayList<>();

    public Integer getIdReceta() { return idReceta; }
    public void setIdReceta(Integer v) { this.idReceta = v; }
    public String getTipoProducto() { return tipoProducto; }
    public void setTipoProducto(String v) { this.tipoProducto = v; }
    public String getNombre() { return nombre; }
    public void setNombre(String v) { this.nombre = v; }
    public String getNotas() { return notas; }
    public void setNotas(String v) { this.notas = v; }
    public boolean isActiva() { return activa; }
    public void setActiva(boolean v) { this.activa = v; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime v) { this.fechaRegistro = v; }
    public List<RecetaIngrediente> getIngredientes() { return ingredientes; }

    @Override public String toString() {
        return tipoProducto + (nombre == null ? "" : " — " + nombre);
    }
}
