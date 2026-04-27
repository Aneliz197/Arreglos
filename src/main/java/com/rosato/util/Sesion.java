package com.rosato.util;

/**
 * Sesión global muy simple. Para una app de escritorio con un solo usuario
 * activo es suficiente mantenerla en memoria estática.
 */
public final class Sesion {

    public enum Rol { CLIENTE, EMPLEADO }

    private static Integer idUsuario;
    private static String nombreMostrar;
    private static Rol rol;
    private static String areaTrabajo; // solo para empleados

    private Sesion() { }

    public static void iniciarCliente(int idCliente, String nombre) {
        idUsuario = idCliente;
        nombreMostrar = nombre;
        rol = Rol.CLIENTE;
        areaTrabajo = null;
    }

    public static void iniciarEmpleado(int idEmpleado, String nombre, String area) {
        idUsuario = idEmpleado;
        nombreMostrar = nombre;
        rol = Rol.EMPLEADO;
        areaTrabajo = area;
    }

    public static void cerrar() {
        idUsuario = null;
        nombreMostrar = null;
        rol = null;
        areaTrabajo = null;
    }

    public static Integer getIdUsuario() { return idUsuario; }
    public static String getNombreMostrar() { return nombreMostrar; }
    public static Rol getRol() { return rol; }
    public static String getAreaTrabajo() { return areaTrabajo; }
    public static boolean esEmpleado() { return rol == Rol.EMPLEADO; }
    public static boolean esCliente() { return rol == Rol.CLIENTE; }
    public static boolean esAdministrador() {
        return esEmpleado() && "Administrador".equalsIgnoreCase(areaTrabajo);
    }
}
