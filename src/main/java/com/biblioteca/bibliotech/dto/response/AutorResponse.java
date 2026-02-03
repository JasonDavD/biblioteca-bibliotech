package com.biblioteca.bibliotech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para autores.
 * Incluye información adicional como cantidad de libros.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorResponse {
    
    private Long id;
    private String nombre;
    private String nacionalidad;
    private LocalDateTime fechaRegistro;
    
    // Campo calculado: cantidad de libros del autor
    private Long cantidadLibros;
}
