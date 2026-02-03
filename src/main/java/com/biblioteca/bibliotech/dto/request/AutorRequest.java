package com.biblioteca.bibliotech.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear o actualizar autores.
 * Usado por EMPLEADO para gestionar el catálogo de autores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutorRequest {
    
    @NotBlank(message = "El nombre del autor es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @Size(max = 50, message = "La nacionalidad no puede exceder 50 caracteres")
    private String nacionalidad;
}
