package com.biblioteca.bibliotech.entity;

import com.biblioteca.bibliotech.enums.Rol;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa a los usuarios del sistema (Empleados y Administradores).
 * Esta tabla es exclusiva para el acceso al software, NO para los clientes/socios.
 */
@Entity
@Table(name = "usuarios_sistema")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioSistema {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario_sis")
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password; // Texto plano según requerimiento
    
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;
    
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
    
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
