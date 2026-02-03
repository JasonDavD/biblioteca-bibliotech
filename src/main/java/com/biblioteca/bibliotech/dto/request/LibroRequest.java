package com.biblioteca.bibliotech.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear o actualizar libros.
 * Usado por EMPLEADO para gestionar el catálogo de libros.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibroRequest {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 1, max = 200, message = "El título debe tener entre 1 y 200 caracteres")
    private String titulo;
    
    @NotBlank(message = "El ISBN es obligatorio")
    @Size(min = 10, max = 20, message = "El ISBN debe tener entre 10 y 20 caracteres")
    private String isbn;
    
    @Min(value = 1000, message = "El año de publicación debe ser mayor a 1000")
    @Max(value = 2100, message = "El año de publicación no puede ser mayor a 2100")
    private Integer anioPublicacion;
    
    @NotNull(message = "La cantidad total es obligatoria")
    @Min(value = 0, message = "La cantidad total no puede ser negativa")
    private Integer cantidadTotal;
    
    @NotNull(message = "El autor es obligatorio")
    private Long idAutor;
    
    @NotNull(message = "La categoría es obligatoria")
    private Long idCategoria;
}
