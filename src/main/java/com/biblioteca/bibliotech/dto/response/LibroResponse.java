package com.biblioteca.bibliotech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para libros.
 * Incluye información del autor y categoría de forma plana.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroResponse {
    
    private Long id;
    private String titulo;
    private String isbn;
    private Integer anioPublicacion;
    private Integer cantidadTotal;
    private Integer cantidadDisponible;
    private LocalDateTime ultimaActualizacion;
    
    // Datos del autor (aplanados)
    private Long idAutor;
    private String nombreAutor;
    
    // Datos de la categoría (aplanados)
    private Long idCategoria;
    private String nombreCategoria;
    
    // Campo calculado: indica si hay disponibilidad
    private Boolean disponible;
    
    // Campo calculado: cantidad prestada actualmente
    private Integer cantidadPrestada;
}
