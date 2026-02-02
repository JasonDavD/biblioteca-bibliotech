package com.biblioteca.bibliotech.repository;

import com.biblioteca.bibliotech.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de autores.
 */
@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    
    /**
     * Busca un autor por nombre exacto.
     */
    Optional<Autor> findByNombre(String nombre);
    
    /**
     * Busca autores cuyo nombre contenga el texto dado.
     */
    List<Autor> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Busca autores por nacionalidad.
     */
    List<Autor> findByNacionalidad(String nacionalidad);
    
    /**
     * Obtiene todos los autores ordenados por nombre.
     */
    List<Autor> findAllByOrderByNombreAsc();
    
    /**
     * Verifica si existe un autor con el nombre dado.
     */
    boolean existsByNombreIgnoreCase(String nombre);
    
    /**
     * Obtiene autores que tienen al menos un libro registrado.
     */
    @Query("SELECT DISTINCT a FROM Autor a JOIN a.libros l")
    List<Autor> findAutoresConLibros();
    
    /**
     * Obtiene las nacionalidades distintas de los autores.
     */
    @Query("SELECT DISTINCT a.nacionalidad FROM Autor a WHERE a.nacionalidad IS NOT NULL ORDER BY a.nacionalidad")
    List<String> findNacionalidadesDistintas();
    
    /**
     * Cuenta cuántos libros tiene un autor.
     */
    @Query("SELECT COUNT(l) FROM Libro l WHERE l.autor.id = :autorId")
    Long countLibrosByAutorId(@Param("autorId") Long autorId);
}
