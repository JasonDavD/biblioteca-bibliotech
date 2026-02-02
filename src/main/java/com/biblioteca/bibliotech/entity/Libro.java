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
 * Entidad que representa los libros de la biblioteca.
 * Incluye control de stock con cantidad_total y cantidad_disponible.
 */
@Entity
@Table(name = "libros")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Long id;
    
    @Column(name = "titulo", nullable = false, length = 200)
    private String titulo;
    
    @Column(name = "isbn", nullable = false, unique = true, length = 20)
    private String isbn;
    
    @Column(name = "anio_publicacion")
    private Integer anioPublicacion;
    
    @Column(name = "cantidad_total", nullable = false)
    @Builder.Default
    private Integer cantidadTotal = 0;
    
    @Column(name = "cantidad_disponible", nullable = false)
    @Builder.Default
    private Integer cantidadDisponible = 0;
    
    // Relación con Autor (Muchos libros pueden tener un autor)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_autor")
    private Autor autor;
    
    // Relación con Categoría (Muchos libros pueden tener una categoría)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;
    
    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;
    
    // Relación con préstamos
    @OneToMany(mappedBy = "libro", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prestamo> prestamos = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.ultimaActualizacion = LocalDateTime.now();
    }
    
    /**
     * Verifica si hay ejemplares disponibles para préstamo.
     */
    public boolean tieneDisponibilidad() {
        return this.cantidadDisponible > 0;
    }
    
    /**
     * Reduce el stock disponible en 1 unidad (al realizar préstamo).
     */
    public void reducirStock() {
        if (this.cantidadDisponible > 0) {
            this.cantidadDisponible--;
        }
    }
    
    /**
     * Aumenta el stock disponible en 1 unidad (al devolver).
     */
    public void aumentarStock() {
        if (this.cantidadDisponible < this.cantidadTotal) {
            this.cantidadDisponible++;
        }
    }
}
