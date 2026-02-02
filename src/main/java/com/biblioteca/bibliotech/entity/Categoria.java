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
 * Entidad que representa las categorías o géneros de los libros.
 */
@Entity
@Table(name = "categorias")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long id;
    
    @Column(name = "nombre", nullable = false, unique = true, length = 50)
    private String nombre;
    
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
    
    // Relación con libros (Una categoría puede tener muchos libros)
    @OneToMany(mappedBy = "categoria", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Libro> libros = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}
