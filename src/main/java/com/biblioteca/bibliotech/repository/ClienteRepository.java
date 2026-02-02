package com.biblioteca.bibliotech.repository;

import com.biblioteca.bibliotech.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de clientes (socios/lectores).
 * Incluye métodos para validación de estado y búsquedas.
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    /**
     * Busca un cliente por DNI.
     */
    Optional<Cliente> findByDni(String dni);
    
    /**
     * Verifica si existe un cliente con el DNI dado.
     */
    boolean existsByDni(String dni);
    
    /**
     * Busca un cliente por email.
     */
    Optional<Cliente> findByEmail(String email);
    
    /**
     * Verifica si existe un cliente con el email dado.
     */
    boolean existsByEmail(String email);
    
    /**
     * Obtiene todos los clientes activos.
     */
    List<Cliente> findByActivoTrue();
    
    /**
     * Obtiene todos los clientes inactivos.
     */
    List<Cliente> findByActivoFalse();
    
    /**
     * Obtiene todos los clientes ordenados por apellido y nombre.
     */
    List<Cliente> findAllByOrderByApellidoAscNombreAsc();
    
    /**
     * Busca clientes por nombre o apellido (búsqueda parcial).
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(c.dni) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Cliente> buscarPorTermino(@Param("termino") String termino);
    
    /**
     * Busca clientes activos por nombre o apellido.
     */
    @Query("SELECT c FROM Cliente c " +
           "WHERE c.activo = true " +
           "AND (LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(c.apellido) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(c.dni) LIKE LOWER(CONCAT('%', :termino, '%')))")
    List<Cliente> buscarActivosPorTermino(@Param("termino") String termino);
    
    /**
     * Cuenta el total de clientes activos.
     */
    Long countByActivoTrue();
    
    /**
     * Cuenta el total de clientes inactivos.
     */
    Long countByActivoFalse();
    
    /**
     * Obtiene clientes que tienen préstamos activos.
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "JOIN c.prestamos p " +
           "WHERE p.estado = 'ACTIVO'")
    List<Cliente> findClientesConPrestamosActivos();
    
    /**
     * Obtiene clientes que tienen préstamos vencidos.
     */
    @Query("SELECT DISTINCT c FROM Cliente c " +
           "JOIN c.prestamos p " +
           "WHERE p.estado = 'VENCIDO'")
    List<Cliente> findClientesConPrestamosVencidos();
}
