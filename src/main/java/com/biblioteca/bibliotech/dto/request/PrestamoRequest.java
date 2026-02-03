package com.biblioteca.bibliotech.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para crear un nuevo préstamo.
 * Usado por EMPLEADO para registrar préstamos de libros.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoRequest {
    
    @NotNull(message = "El libro es obligatorio")
    private Long idLibro;
    
    @NotNull(message = "El cliente es obligatorio")
    private Long idCliente;
    
    @NotNull(message = "La fecha de devolución esperada es obligatoria")
    @Future(message = "La fecha de devolución debe ser una fecha futura")
    private LocalDate fechaDevolucionEsperada;
    
    @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
    private String observaciones;
}
