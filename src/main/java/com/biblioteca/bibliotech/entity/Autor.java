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
 * Entidad que representa a los autores de los libros.
 */
@Entity
@Table(name = "autores")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Autor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_autor")
    private Long id;
    
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    
    @Column(name = "nacionalidad", length = 50)
    private String nacionalidad;
    
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
    
    // Relación con libros (Un autor puede tener muchos libros)
    @OneToMany(mappedBy = "autor", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Libro> libros = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
