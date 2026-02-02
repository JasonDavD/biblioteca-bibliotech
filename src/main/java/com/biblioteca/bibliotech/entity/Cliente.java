package com.biblioteca.bibliotech.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa a los clientes/socios de la biblioteca.
 * Esta tabla es independiente de los usuarios del sistema (empleados/admin).
 * Los clientes son quienes pueden solicitar préstamos de libros.
 */
@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;
    
    @Column(name = "dni", nullable = false, unique = true, length = 8)
    private String dni;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "apellido", nullable = false, length = 100)
    private String apellido;
    
    @Column(name = "email", unique = true, length = 100)
    private String email;
    
    @Column(name = "telefono", length = 20)
    private String telefono;
    
    @Column(name = "direccion", length = 255)
    private String direccion;
    
    @Column(name = "activo")
    @Builder.Default
    private Boolean activo = true;
    
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
    
    // Relación con préstamos
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prestamo> prestamos = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
    
    /**
     * Retorna el nombre completo del cliente.
     */
    public String getNombreCompleto() {
        return this.nombre + " " + this.apellido;
    }
    
    /**
     * Verifica si el cliente puede realizar préstamos.
     */
    public boolean puedeRealizarPrestamo() {
        return Boolean.TRUE.equals(this.activo);
    }
}
