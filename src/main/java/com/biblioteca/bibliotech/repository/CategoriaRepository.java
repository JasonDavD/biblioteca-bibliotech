package com.biblioteca.bibliotech.repository;

import com.biblioteca.bibliotech.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de categorías de libros.
 */
@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    
    /**
     * Busca una categoría por nombre exacto.
     */
    Optional<Categoria> findByNombre(String nombre);
    
    /**
     * Busca categorías cuyo nombre contenga el texto dado.
     */
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
    
    /**
     * Obtiene todas las categorías ordenadas por nombre.
     */
    List<Categoria> findAllByOrderByNombreAsc();
    
    /**
     * Verifica si existe una categoría con el nombre dado.
     */
    boolean existsByNombreIgnoreCase(String nombre);
    
    /**
     * Obtiene categorías que tienen al menos un libro registrado.
     */
    @Query("SELECT DISTINCT c FROM Categoria c JOIN c.libros l")
    List<Categoria> findCategoriasConLibros();
    
    /**
     * Cuenta cuántos libros tiene una categoría.
     */
    @Query("SELECT COUNT(l) FROM Libro l WHERE l.categoria.id = :categoriaId")
    Long countLibrosByCategoriaId(@Param("categoriaId") Long categoriaId);
}
