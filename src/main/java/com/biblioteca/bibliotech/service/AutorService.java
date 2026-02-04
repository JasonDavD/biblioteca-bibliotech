package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.request.AutorRequest;
import com.biblioteca.bibliotech.dto.response.AutorResponse;
import com.biblioteca.bibliotech.entity.Autor;
import com.biblioteca.bibliotech.exception.BusinessException;
import com.biblioteca.bibliotech.exception.ResourceNotFoundException;
import com.biblioteca.bibliotech.mapper.AutorMapper;
import com.biblioteca.bibliotech.repository.AutorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de autores.
 * Accesible por ADMIN y EMPLEADO.
 */
@Service
@Transactional
public class AutorService {
    
    private final AutorRepository autorRepository;
    private final AutorMapper autorMapper;
    
    public AutorService(AutorRepository autorRepository, AutorMapper autorMapper) {
        this.autorRepository = autorRepository;
        this.autorMapper = autorMapper;
    }
    
    /**
     * Obtiene todos los autores.
     */
    @Transactional(readOnly = true)
    public List<AutorResponse> listarTodos() {
        List<Autor> autores = autorRepository.findAllByOrderByNombreAsc();
        return autores.stream()
                .map(this::convertirConCantidadLibros)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene autores que tienen libros registrados.
     */
    @Transactional(readOnly = true)
    public List<AutorResponse> listarConLibros() {
        List<Autor> autores = autorRepository.findAutoresConLibros();
        return autores.stream()
                .map(this::convertirConCantidadLibros)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca un autor por su ID.
     */
    @Transactional(readOnly = true)
    public AutorResponse buscarPorId(Long id) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", id));
        return convertirConCantidadLibros(autor);
    }
    
    /**
     * Busca un autor por nombre exacto.
     */
    @Transactional(readOnly = true)
    public AutorResponse buscarPorNombre(String nombre) {
        Autor autor = autorRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "nombre", nombre));
        return convertirConCantidadLibros(autor);
    }
    
    /**
     * Busca autores por nombre (búsqueda parcial).
     */
    @Transactional(readOnly = true)
    public List<AutorResponse> buscarPorNombreParcial(String nombre) {
        List<Autor> autores = autorRepository.findByNombreContainingIgnoreCase(nombre);
        return autores.stream()
                .map(this::convertirConCantidadLibros)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca autores por nacionalidad.
     */
    @Transactional(readOnly = true)
    public List<AutorResponse> buscarPorNacionalidad(String nacionalidad) {
        List<Autor> autores = autorRepository.findByNacionalidad(nacionalidad);
        return autores.stream()
                .map(this::convertirConCantidadLibros)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene todas las nacionalidades distintas.
     */
    @Transactional(readOnly = true)
    public List<String> listarNacionalidades() {
        return autorRepository.findNacionalidadesDistintas();
    }
    
    /**
     * Crea un nuevo autor.
     */
    public AutorResponse crear(AutorRequest request) {
        // Validar que el nombre no exista
        if (autorRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un autor con el nombre: " + request.getNombre()
            );
        }
        
        Autor autor = autorMapper.toEntity(request);
        autor = autorRepository.save(autor);
        
        return convertirConCantidadLibros(autor);
    }
    
    /**
     * Actualiza un autor existente.
     */
    public AutorResponse actualizar(Long id, AutorRequest request) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", id));
        
        // Validar nombre único (si cambió)
        if (!autor.getNombre().equalsIgnoreCase(request.getNombre()) 
                && autorRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un autor con el nombre: " + request.getNombre()
            );
        }
        
        autorMapper.updateEntityFromRequest(request, autor);
        autor = autorRepository.save(autor);
        
        return convertirConCantidadLibros(autor);
    }
    
    /**
     * Elimina un autor.
     */
    public void eliminar(Long id) {
        Autor autor = autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", id));
        
        // Verificar que no tenga libros asociados
        Long cantidadLibros = autorRepository.countLibrosByAutorId(id);
        if (cantidadLibros > 0) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "No se puede eliminar el autor porque tiene " + cantidadLibros + " libro(s) asociado(s)"
            );
        }
        
        autorRepository.delete(autor);
    }
    
    /**
     * Cuenta el total de autores.
     */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return autorRepository.count();
    }
    
    /**
     * Obtiene la entidad Autor para uso interno.
     */
    @Transactional(readOnly = true)
    public Autor obtenerEntidadPorId(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", id));
    }
    
    /**
     * Convierte una entidad Autor a Response incluyendo cantidad de libros.
     */
    private AutorResponse convertirConCantidadLibros(Autor autor) {
        AutorResponse response = autorMapper.toResponse(autor);
        Long cantidadLibros = autorRepository.countLibrosByAutorId(autor.getId());
        response.setCantidadLibros(cantidadLibros);
        return response;
    }
}
