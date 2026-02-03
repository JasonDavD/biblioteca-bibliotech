package com.biblioteca.bibliotech.dto.request;

import com.biblioteca.bibliotech.enums.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear o actualizar usuarios del sistema.
 * Usado por el ADMIN para gestionar empleados y otros administradores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSistemaRequest {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 50, message = "El username debe tener entre 3 y 50 caracteres")
    private String username;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 4, max = 255, message = "La contraseña debe tener entre 4 y 255 caracteres")
    private String password;
    
    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}
