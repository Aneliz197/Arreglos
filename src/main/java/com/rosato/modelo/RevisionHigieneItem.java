package com.rosato.modelo;

public class RevisionHigieneItem {
    private Integer idItem;
    private int fkIdRevision;
    private String descripcion;
    private boolean cumple;
    private String observacion;

    public RevisionHigieneItem() { }

    public RevisionHigieneItem(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getIdItem() { return idItem; }
    public void setIdItem(Integer v) { this.idItem = v; }
    public int getFkIdRevision() { return fkIdRevision; }
    public void setFkIdRevision(int v) { this.fkIdRevision = v; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String v) { this.descripcion = v; }
    public boolean isCumple() { return cumple; }
    public void setCumple(boolean v) { this.cumple = v; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String v) { this.observacion = v; }
}
