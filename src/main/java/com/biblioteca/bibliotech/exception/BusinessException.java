package com.biblioteca.bibliotech.exception;

/**
 * Excepción lanzada cuando se viola una regla de negocio.
 * Por ejemplo: Cliente inactivo intenta prestar, límite de préstamos excedido, etc.
 */
public class BusinessException extends RuntimeException {
    
    private final String codigo;
    
    public BusinessException(String mensaje) {
        super(mensaje);
        this.codigo = "BUSINESS_ERROR";
    }
    
    public BusinessException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    // Códigos predefinidos para errores comunes
    public static final String CLIENTE_INACTIVO = "CLIENTE_INACTIVO";
    public static final String LIMITE_PRESTAMOS = "LIMITE_PRESTAMOS";
    public static final String SIN_STOCK = "SIN_STOCK";
    public static final String LIBRO_YA_PRESTADO = "LIBRO_YA_PRESTADO";
    public static final String PRESTAMO_NO_ACTIVO = "PRESTAMO_NO_ACTIVO";
    public static final String DUPLICADO = "DUPLICADO";
    public static final String OPERACION_NO_PERMITIDA = "OPERACION_NO_PERMITIDA";
}
