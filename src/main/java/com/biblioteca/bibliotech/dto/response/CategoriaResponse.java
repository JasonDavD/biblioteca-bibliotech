package com.biblioteca.bibliotech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para categorías.
 * Incluye información adicional como cantidad de libros.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponse {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaRegistro;
    
    // Campo calculado: cantidad de libros en la categoría
    private Long cantidadLibros;
}
