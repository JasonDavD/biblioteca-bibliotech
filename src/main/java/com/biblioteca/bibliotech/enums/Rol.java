package com.biblioteca.bibliotech.enums;

/**
 * Roles disponibles para los usuarios del sistema.
 * ADMIN: Acceso total, puede gestionar usuarios y ver reportes.
 * EMPLEADO: Acceso limitado a gestión de libros, clientes y préstamos.
 */
public enum Rol {
    ADMIN("Administrador"),
    EMPLEADO("Empleado");
    
    private final String descripcion;
    
    Rol(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
