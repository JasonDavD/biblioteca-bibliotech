package com.biblioteca.bibliotech.exception;

/**
 * Excepción lanzada cuando hay problemas de autenticación o autorización.
 * Por ejemplo: Credenciales inválidas, acceso denegado por rol, sesión expirada.
 */
public class UnauthorizedException extends RuntimeException {
    
    private final String tipo;
    
    public UnauthorizedException(String mensaje) {
        super(mensaje);
        this.tipo = "UNAUTHORIZED";
    }
    
    public UnauthorizedException(String tipo, String mensaje) {
        super(mensaje);
        this.tipo = tipo;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    // Tipos predefinidos
    public static final String CREDENCIALES_INVALIDAS = "CREDENCIALES_INVALIDAS";
    public static final String SESION_EXPIRADA = "SESION_EXPIRADA";
    public static final String ACCESO_DENEGADO = "ACCESO_DENEGADO";
    public static final String SIN_SESION = "SIN_SESION";
}
