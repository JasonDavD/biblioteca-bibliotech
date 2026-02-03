package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.request.UsuarioSistemaRequest;
import com.biblioteca.bibliotech.dto.response.UsuarioSistemaResponse;
import com.biblioteca.bibliotech.entity.UsuarioSistema;
import com.biblioteca.bibliotech.enums.Rol;
import com.biblioteca.bibliotech.exception.BusinessException;
import com.biblioteca.bibliotech.exception.ResourceNotFoundException;
import com.biblioteca.bibliotech.mapper.UsuarioSistemaMapper;
import com.biblioteca.bibliotech.repository.UsuarioSistemaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de usuarios del sistema.
 * Solo accesible por usuarios con rol ADMIN.
 */
@Service
@Transactional
public class UsuarioSistemaService {
    
    private final UsuarioSistemaRepository usuarioRepository;
    private final UsuarioSistemaMapper usuarioMapper;
    
    public UsuarioSistemaService(UsuarioSistemaRepository usuarioRepository,
                                  UsuarioSistemaMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }
    
    /**
     * Obtiene todos los usuarios del sistema.
     */
    @Transactional(readOnly = true)
    public List<UsuarioSistemaResponse> listarTodos() {
        List<UsuarioSistema> usuarios = usuarioRepository.findAllByOrderByNombreAsc();
        return usuarioMapper.toResponseList(usuarios);
    }
    
    /**
     * Obtiene usuarios por rol.
     */
    @Transactional(readOnly = true)
    public List<UsuarioSistemaResponse> listarPorRol(Rol rol) {
        List<UsuarioSistema> usuarios = usuarioRepository.findByRol(rol);
        return usuarioMapper.toResponseList(usuarios);
    }
    
    /**
     * Busca un usuario por su ID.
     */
    @Transactional(readOnly = true)
    public UsuarioSistemaResponse buscarPorId(Long id) {
        UsuarioSistema usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        return usuarioMapper.toResponse(usuario);
    }
    
    /**
     * Busca un usuario por su username.
     */
    @Transactional(readOnly = true)
    public UsuarioSistemaResponse buscarPorUsername(String username) {
        UsuarioSistema usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "username", username));
        return usuarioMapper.toResponse(usuario);
    }
    
    /**
     * Busca usuarios por nombre (búsqueda parcial).
     */
    @Transactional(readOnly = true)
    public List<UsuarioSistemaResponse> buscarPorNombre(String nombre) {
        List<UsuarioSistema> usuarios = usuarioRepository.findByNombreContainingIgnoreCase(nombre);
        return usuarioMapper.toResponseList(usuarios);
    }
    
    /**
     * Crea un nuevo usuario del sistema.
     */
    public UsuarioSistemaResponse crear(UsuarioSistemaRequest request) {
        // Validar que el username no exista
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un usuario con el username: " + request.getUsername()
            );
        }
        
        UsuarioSistema usuario = usuarioMapper.toEntity(request);
        usuario = usuarioRepository.save(usuario);
        
        return usuarioMapper.toResponse(usuario);
    }
    
    /**
     * Actualiza un usuario existente.
     */
    public UsuarioSistemaResponse actualizar(Long id, UsuarioSistemaRequest request) {
        UsuarioSistema usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        // Validar que el nuevo username no esté en uso por otro usuario
        if (!usuario.getUsername().equals(request.getUsername()) 
                && usuarioRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un usuario con el username: " + request.getUsername()
            );
        }
        
        usuarioMapper.updateEntityFromRequest(request, usuario);
        usuario = usuarioRepository.save(usuario);
        
        return usuarioMapper.toResponse(usuario);
    }
    
    /**
     * Actualiza solo la contraseña de un usuario.
     */
    public void cambiarPassword(Long id, String nuevaPassword) {
        UsuarioSistema usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
        
        usuario.setPassword(nuevaPassword);
        usuarioRepository.save(usuario);
    }
    
    /**
     * Elimina un usuario del sistema.
     */
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario", "id", id);
        }
        
        // Validar que no sea el último administrador
        UsuarioSistema usuario = usuarioRepository.findById(id).get();
        if (Rol.ADMIN.equals(usuario.getRol())) {
            long countAdmins = usuarioRepository.findByRol(Rol.ADMIN).size();
            if (countAdmins <= 1) {
                throw new BusinessException(
                        BusinessException.OPERACION_NO_PERMITIDA,
                        "No se puede eliminar el último administrador del sistema"
                );
            }
        }
        
        usuarioRepository.deleteById(id);
    }
    
    /**
     * Cuenta el total de usuarios.
     */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return usuarioRepository.count();
    }
    
    /**
     * Cuenta usuarios por rol.
     */
    @Transactional(readOnly = true)
    public long contarPorRol(Rol rol) {
        return usuarioRepository.findByRol(rol).size();
    }
    
    /**
     * Obtiene la entidad Usuario para uso interno (login).
     */
    @Transactional(readOnly = true)
    public UsuarioSistema obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));
    }
}
