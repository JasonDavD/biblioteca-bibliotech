package com.biblioteca.bibliotech.repository;

import com.biblioteca.bibliotech.entity.UsuarioSistema;
import com.biblioteca.bibliotech.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de usuarios del sistema (Empleados y Administradores).
 * Proporciona métodos para autenticación y administración de usuarios.
 */
@Repository
public interface UsuarioSistemaRepository extends JpaRepository<UsuarioSistema, Long> {
    
    /**
     * Busca un usuario por su username.
     * Utilizado principalmente para el login.
     */
    Optional<UsuarioSistema> findByUsername(String username);
    
    /**
     * Busca un usuario por username y password.
     * Método directo para validación de login.
     */
    Optional<UsuarioSistema> findByUsernameAndPassword(String username, String password);
    
    /**
     * Verifica si existe un usuario con el username dado.
     * Útil para validar duplicados al crear usuarios.
     */
    boolean existsByUsername(String username);
    
    /**
     * Obtiene todos los usuarios de un rol específico.
     */
    List<UsuarioSistema> findByRol(Rol rol);
    
    /**
     * Obtiene todos los usuarios ordenados por nombre.
     */
    List<UsuarioSistema> findAllByOrderByNombreAsc();
    
    /**
     * Busca usuarios cuyo nombre contenga el texto dado (búsqueda parcial).
     */
    List<UsuarioSistema> findByNombreContainingIgnoreCase(String nombre);
}
