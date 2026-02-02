package com.biblioteca.bibliotech.repository;

import com.biblioteca.bibliotech.entity.Prestamo;
import com.biblioteca.bibliotech.enums.EstadoPrestamo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para gestión de préstamos.
 * Incluye métodos críticos para las reglas de negocio:
 * - Validación de límite de préstamos por cliente
 * - Búsqueda de préstamos vencidos
 * - Estadísticas de préstamos
 */
@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    
    // ==================== BÚSQUEDAS POR ESTADO ====================
    
    /**
     * Obtiene todos los préstamos por estado.
     */
    List<Prestamo> findByEstado(EstadoPrestamo estado);
    
    /**
     * Obtiene préstamos activos ordenados por fecha de devolución esperada.
     */
    @Query("SELECT p FROM Prestamo p WHERE p.estado = 'ACTIVO' ORDER BY p.fechaDevolucionEsperada ASC")
    List<Prestamo> findPrestamosActivos();
    
    /**
     * Obtiene préstamos vencidos (estado VENCIDO o activos con fecha pasada).
     */
    @Query("SELECT p FROM Prestamo p " +
           "WHERE p.estado = 'VENCIDO' " +
           "OR (p.estado = 'ACTIVO' AND p.fechaDevolucionEsperada < CURRENT_DATE) " +
           "ORDER BY p.fechaDevolucionEsperada ASC")
    List<Prestamo> findPrestamosVencidos();
    
    // ==================== BÚSQUEDAS POR CLIENTE ====================
    
    /**
     * Obtiene todos los préstamos de un cliente.
     */
    List<Prestamo> findByClienteId(Long clienteId);
    
    /**
     * Obtiene los préstamos activos de un cliente.
     */
    @Query("SELECT p FROM Prestamo p WHERE p.cliente.id = :clienteId AND p.estado = 'ACTIVO'")
    List<Prestamo> findPrestamosActivosByClienteId(@Param("clienteId") Long clienteId);
    
    /**
     * Cuenta los préstamos activos de un cliente.
     * CRÍTICO: Usado para validar el límite de 3 préstamos.
     */
    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.cliente.id = :clienteId AND p.estado = 'ACTIVO'")
    Long countPrestamosActivosByClienteId(@Param("clienteId") Long clienteId);
    
    /**
     * Verifica si el cliente tiene el libro actualmente prestado.
     */
    @Query("SELECT COUNT(p) > 0 FROM Prestamo p " +
           "WHERE p.cliente.id = :clienteId " +
           "AND p.libro.id = :libroId " +
           "AND p.estado = 'ACTIVO'")
    boolean existsPrestamoActivoByClienteIdAndLibroId(
            @Param("clienteId") Long clienteId, 
            @Param("libroId") Long libroId);
    
    // ==================== BÚSQUEDAS POR LIBRO ====================
    
    /**
     * Obtiene todos los préstamos de un libro.
     */
    List<Prestamo> findByLibroId(Long libroId);
    
    /**
     * Obtiene los préstamos activos de un libro.
     */
    @Query("SELECT p FROM Prestamo p WHERE p.libro.id = :libroId AND p.estado = 'ACTIVO'")
    List<Prestamo> findPrestamosActivosByLibroId(@Param("libroId") Long libroId);
    
    // ==================== BÚSQUEDAS POR FECHA ====================
    
    /**
     * Obtiene préstamos realizados en un rango de fechas.
     */
    @Query("SELECT p FROM Prestamo p " +
           "WHERE p.fechaPrestamo BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY p.fechaPrestamo DESC")
    List<Prestamo> findByFechaPrestamoBetween(
            @Param("fechaInicio") LocalDate fechaInicio, 
            @Param("fechaFin") LocalDate fechaFin);
    
    /**
     * Obtiene préstamos que vencen en una fecha específica.
     */
    List<Prestamo> findByFechaDevolucionEsperadaAndEstado(
            LocalDate fechaDevolucion, 
            EstadoPrestamo estado);
    
    /**
     * Obtiene préstamos que vencen pronto (próximos N días).
     */
    @Query("SELECT p FROM Prestamo p " +
           "WHERE p.estado = 'ACTIVO' " +
           "AND p.fechaDevolucionEsperada BETWEEN CURRENT_DATE AND :fechaLimite " +
           "ORDER BY p.fechaDevolucionEsperada ASC")
    List<Prestamo> findPrestamosPorVencer(@Param("fechaLimite") LocalDate fechaLimite);
    
    // ==================== ACTUALIZACIONES ====================
    
    /**
     * Actualiza el estado de préstamos vencidos.
     * Cambia de ACTIVO a VENCIDO los que pasaron la fecha de devolución.
     */
    @Modifying
    @Query("UPDATE Prestamo p SET p.estado = 'VENCIDO' " +
           "WHERE p.estado = 'ACTIVO' " +
           "AND p.fechaDevolucionEsperada < CURRENT_DATE")
    int actualizarPrestamosVencidos();
    
    // ==================== ESTADÍSTICAS ====================
    
    /**
     * Cuenta préstamos por estado.
     */
    Long countByEstado(EstadoPrestamo estado);
    
    /**
     * Cuenta préstamos realizados hoy.
     */
    @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.fechaPrestamo = CURRENT_DATE")
    Long countPrestamosHoy();
    
    /**
     * Cuenta devoluciones realizadas hoy.
     */
    @Query("SELECT COUNT(p) FROM Prestamo p " +
           "WHERE p.fechaDevolucionReal = CURRENT_DATE " +
           "AND p.estado = 'DEVUELTO'")
    Long countDevolucionesHoy();
    
    /**
     * Obtiene los préstamos más recientes.
     */
    @Query("SELECT p FROM Prestamo p ORDER BY p.fechaRegistro DESC")
    List<Prestamo> findPrestamosRecientes();
    
    // ==================== REPORTES ====================
    
    /**
     * Obtiene préstamos vencidos con información completa para reportes.
     * Incluye datos del libro y cliente.
     */
    @Query("SELECT p FROM Prestamo p " +
           "JOIN FETCH p.libro l " +
           "JOIN FETCH p.cliente c " +
           "WHERE p.estado = 'VENCIDO' " +
           "OR (p.estado = 'ACTIVO' AND p.fechaDevolucionEsperada < CURRENT_DATE) " +
           "ORDER BY p.fechaDevolucionEsperada ASC")
    List<Prestamo> findPrestamosVencidosConDetalles();
    
    /**
     * Obtiene historial de préstamos de un cliente con detalles.
     */
    @Query("SELECT p FROM Prestamo p " +
           "JOIN FETCH p.libro l " +
           "WHERE p.cliente.id = :clienteId " +
           "ORDER BY p.fechaPrestamo DESC")
    List<Prestamo> findHistorialByClienteId(@Param("clienteId") Long clienteId);
}
