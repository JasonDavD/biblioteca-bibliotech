package com.biblioteca.bibliotech.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para clientes (socios/lectores).
 * Incluye información adicional sobre préstamos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClienteResponse {
    
    private Long id;
    private String dni;
    private String nombre;
    private String apellido;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String direccion;
    private Boolean activo;
    private String estadoDescripcion;
    private LocalDateTime fechaRegistro;
    
    // Campos calculados: información de préstamos
    private Long prestamosActivos;
    private Long prestamosVencidos;
    private Long totalPrestamos;
    
    // Indica si puede realizar más préstamos (límite de 3)
    private Boolean puedePrestar;
}
