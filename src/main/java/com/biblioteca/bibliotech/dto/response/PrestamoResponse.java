package com.biblioteca.bibliotech.dto.response;

import com.biblioteca.bibliotech.enums.EstadoPrestamo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para préstamos.
 * Incluye información del libro y cliente de forma plana.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PrestamoResponse {
    
    private Long id;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucionEsperada;
    private LocalDate fechaDevolucionReal;
    private EstadoPrestamo estado;
    private String estadoDescripcion;
    private String observaciones;
    private LocalDateTime fechaRegistro;
    
    // Datos del libro (aplanados)
    private Long idLibro;
    private String tituloLibro;
    private String isbnLibro;
    
    // Datos del cliente (aplanados)
    private Long idCliente;
    private String nombreCompletoCliente;
    private String dniCliente;
    
    // Campos calculados
    private Long diasRestantes;      // Días que faltan para devolver (negativo si está vencido)
    private Long diasRetraso;        // Días de retraso (solo si está vencido)
    private Boolean estaVencido;
}
