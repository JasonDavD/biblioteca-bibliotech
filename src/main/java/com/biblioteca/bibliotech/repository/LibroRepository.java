package com.biblioteca.bibliotech.repository;

import com.biblioteca.bibliotech.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de libros.
 * Incluye métodos para control de stock y búsquedas avanzadas.
 */
@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    
    /**
     * Busca un libro por ISBN.
     */
    Optional<Libro> findByIsbn(String isbn);
    
    /**
     * Verifica si existe un libro con el ISBN dado.
     */
    boolean existsByIsbn(String isbn);
    
    /**
     * Busca libros cuyo título contenga el texto dado.
     */
    List<Libro> findByTituloContainingIgnoreCase(String titulo);
    
    /**
     * Busca libros por autor.
     */
    List<Libro> findByAutorId(Long autorId);
    
    /**
     * Busca libros por categoría.
     */
    List<Libro> findByCategoriaId(Long categoriaId);
    
    /**
     * Busca libros por año de publicación.
     */
    List<Libro> findByAnioPublicacion(Integer anio);
    
    /**
     * Obtiene todos los libros ordenados por título.
     */
    List<Libro> findAllByOrderByTituloAsc();
    
    /**
     * Obtiene libros que tienen ejemplares disponibles.
     */
    @Query("SELECT l FROM Libro l WHERE l.cantidadDisponible > 0 ORDER BY l.titulo")
    List<Libro> findLibrosDisponibles();
    
    /**
     * Obtiene libros sin stock disponible.
     */
    @Query("SELECT l FROM Libro l WHERE l.cantidadDisponible = 0 ORDER BY l.titulo")
    List<Libro> findLibrosSinStock();
    
    /**
     * Búsqueda combinada por título, autor o ISBN.
     */
    @Query("SELECT l FROM Libro l " +
           "WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(l.isbn) LIKE LOWER(CONCAT('%', :termino, '%')) " +
           "OR LOWER(l.autor.nombre) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Libro> buscarPorTermino(@Param("termino") String termino);
    
    /**
     * Reduce el stock disponible de un libro en 1 unidad.
     */
    @Modifying
    @Query("UPDATE Libro l SET l.cantidadDisponible = l.cantidadDisponible - 1 " +
           "WHERE l.id = :libroId AND l.cantidadDisponible > 0")
    int reducirStock(@Param("libroId") Long libroId);
    
    /**
     * Aumenta el stock disponible de un libro en 1 unidad.
     */
    @Modifying
    @Query("UPDATE Libro l SET l.cantidadDisponible = l.cantidadDisponible + 1 " +
           "WHERE l.id = :libroId AND l.cantidadDisponible < l.cantidadTotal")
    int aumentarStock(@Param("libroId") Long libroId);
    
    /**
     * Obtiene los libros más prestados (para estadísticas).
     */
    @Query("SELECT l FROM Libro l " +
           "WHERE l.cantidadTotal > l.cantidadDisponible " +
           "ORDER BY (l.cantidadTotal - l.cantidadDisponible) DESC")
    List<Libro> findLibrosMasPrestados();
    
    /**
     * Cuenta el total de ejemplares en la biblioteca.
     */
    @Query("SELECT COALESCE(SUM(l.cantidadTotal), 0) FROM Libro l")
    Long countTotalEjemplares();
    
    /**
     * Cuenta el total de ejemplares disponibles.
     */
    @Query("SELECT COALESCE(SUM(l.cantidadDisponible), 0) FROM Libro l")
    Long countEjemplaresDisponibles();
}
