package com.biblioteca.bibliotech.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado.
 * Por ejemplo: Libro no encontrado, Cliente no encontrado, etc.
 */
public class ResourceNotFoundException extends RuntimeException {
    
    private final String recurso;
    private final String campo;
    private final Object valor;
    
    public ResourceNotFoundException(String recurso, String campo, Object valor) {
        super(String.format("%s no encontrado con %s: '%s'", recurso, campo, valor));
        this.recurso = recurso;
        this.campo = campo;
        this.valor = valor;
    }
    
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
        this.recurso = null;
        this.campo = null;
        this.valor = null;
    }
    
    public String getRecurso() {
        return recurso;
    }
    
    public String getCampo() {
        return campo;
    }
    
    public Object getValor() {
        return valor;
    }
}
