package com.biblioteca.bibliotech.dto.response;

import com.biblioteca.bibliotech.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO de respuesta para usuarios del sistema.
 * No incluye el password por seguridad.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSistemaResponse {
    
    private Long id;
    private String nombre;
    private String username;
    private Rol rol;
    private String rolDescripcion;
    private LocalDateTime fechaRegistro;
}
