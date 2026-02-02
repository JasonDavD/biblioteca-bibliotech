package com.biblioteca.bibliotech.enums;

/**
 * Estados posibles de un préstamo.
 * ACTIVO: El libro está prestado y dentro del plazo.
 * DEVUELTO: El libro fue devuelto correctamente.
 * VENCIDO: El plazo de devolución ha expirado.
 */
public enum EstadoPrestamo {
    ACTIVO("Activo"),
    DEVUELTO("Devuelto"),
    VENCIDO("Vencido");
    
    private final String descripcion;
    
    EstadoPrestamo(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
}
