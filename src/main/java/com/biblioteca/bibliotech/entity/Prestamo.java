package com.biblioteca.bibliotech.entity;

import com.biblioteca.bibliotech.enums.EstadoPrestamo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad que representa los préstamos de libros.
 * Gestiona las transacciones entre la biblioteca y los clientes.
 */
@Entity
@Table(name = "prestamos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prestamo {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Long id;
    
    // Relación con Libro
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_libro", nullable = false)
    private Libro libro;
    
    // Relación con Cliente
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;
    
    @Column(name = "fecha_prestamo", nullable = false)
    private LocalDate fechaPrestamo;
    
    @Column(name = "fecha_devolucion_esperada", nullable = false)
    private LocalDate fechaDevolucionEsperada;
    
    @Column(name = "fecha_devolucion_real")
    private LocalDate fechaDevolucionReal;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    @Builder.Default
    private EstadoPrestamo estado = EstadoPrestamo.ACTIVO;
    
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;
    
    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
        if (this.estado == null) {
            this.estado = EstadoPrestamo.ACTIVO;
        }
    }
    
    /**
     * Verifica si el préstamo está vencido (fecha esperada ya pasó y no se ha devuelto).
     */
    public boolean estaVencido() {
        return this.estado == EstadoPrestamo.ACTIVO 
               && LocalDate.now().isAfter(this.fechaDevolucionEsperada);
    }
    
    /**
     * Verifica si el préstamo está activo.
     */
    public boolean estaActivo() {
        return this.estado == EstadoPrestamo.ACTIVO;
    }
    
    /**
     * Calcula los días de retraso (si aplica).
     */
    public long getDiasRetraso() {
        if (!estaVencido()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            this.fechaDevolucionEsperada, 
            LocalDate.now()
        );
    }
    
    /**
     * Marca el préstamo como devuelto.
     */
    public void marcarComoDevuelto() {
        this.estado = EstadoPrestamo.DEVUELTO;
        this.fechaDevolucionReal = LocalDate.now();
    }
    
    /**
     * Marca el préstamo como vencido.
     */
    public void marcarComoVencido() {
        this.estado = EstadoPrestamo.VENCIDO;
    }
}
