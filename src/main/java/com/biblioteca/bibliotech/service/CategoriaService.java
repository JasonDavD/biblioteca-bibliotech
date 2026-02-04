package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.request.CategoriaRequest;
import com.biblioteca.bibliotech.dto.response.CategoriaResponse;
import com.biblioteca.bibliotech.entity.Categoria;
import com.biblioteca.bibliotech.exception.BusinessException;
import com.biblioteca.bibliotech.exception.ResourceNotFoundException;
import com.biblioteca.bibliotech.mapper.CategoriaMapper;
import com.biblioteca.bibliotech.repository.CategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de categorías.
 * Accesible por ADMIN y EMPLEADO.
 */
@Service
@Transactional
public class CategoriaService {
    
    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;
    
    public CategoriaService(CategoriaRepository categoriaRepository, 
                            CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }
    
    /**
     * Obtiene todas las categorías.
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        List<Categoria> categorias = categoriaRepository.findAllByOrderByNombreAsc();
        return categorias.stream()
                .map(this::convertirConCantidadLibros)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene categorías que tienen libros registrados.
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarConLibros() {
        List<Categoria> categorias = categoriaRepository.findCategoriasConLibros();
        return categorias.stream()
                .map(this::convertirConCantidadLibros)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca una categoría por su ID.
     */
    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
        return convertirConCantidadLibros(categoria);
    }
    
    /**
     * Busca una categoría por nombre exacto.
     */
    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorNombre(String nombre) {
        Categoria categoria = categoriaRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "nombre", nombre));
        return convertirConCantidadLibros(categoria);
    }
    
    /**
     * Busca categorías por nombre (búsqueda parcial).
     */
    @Transactional(readOnly = true)
    public List<CategoriaResponse> buscarPorNombreParcial(String nombre) {
        List<Categoria> categorias = categoriaRepository.findByNombreContainingIgnoreCase(nombre);
        return categorias.stream()
                .map(this::convertirConCantidadLibros)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea una nueva categoría.
     */
    public CategoriaResponse crear(CategoriaRequest request) {
        // Validar que el nombre no exista
        if (categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe una categoría con el nombre: " + request.getNombre()
            );
        }
        
        Categoria categoria = categoriaMapper.toEntity(request);
        categoria = categoriaRepository.save(categoria);
        
        return convertirConCantidadLibros(categoria);
    }
    
    /**
     * Actualiza una categoría existente.
     */
    public CategoriaResponse actualizar(Long id, CategoriaRequest request) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
        
        // Validar nombre único (si cambió)
        if (!categoria.getNombre().equalsIgnoreCase(request.getNombre()) 
                && categoriaRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe una categoría con el nombre: " + request.getNombre()
            );
        }
        
        categoriaMapper.updateEntityFromRequest(request, categoria);
        categoria = categoriaRepository.save(categoria);
        
        return convertirConCantidadLibros(categoria);
    }
    
    /**
     * Elimina una categoría.
     */
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
        
        // Verificar que no tenga libros asociados
        Long cantidadLibros = categoriaRepository.countLibrosByCategoriaId(id);
        if (cantidadLibros > 0) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "No se puede eliminar la categoría porque tiene " + cantidadLibros + " libro(s) asociado(s)"
            );
        }
        
        categoriaRepository.delete(categoria);
    }
    
    /**
     * Cuenta el total de categorías.
     */
    @Transactional(readOnly = true)
    public long contarTodas() {
        return categoriaRepository.count();
    }
    
    /**
     * Obtiene la entidad Categoria para uso interno.
     */
    @Transactional(readOnly = true)
    public Categoria obtenerEntidadPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
    }
    
    /**
     * Convierte una entidad Categoria a Response incluyendo cantidad de libros.
     */
    private CategoriaResponse convertirConCantidadLibros(Categoria categoria) {
        CategoriaResponse response = categoriaMapper.toResponse(categoria);
        Long cantidadLibros = categoriaRepository.countLibrosByCategoriaId(categoria.getId());
        response.setCantidadLibros(cantidadLibros);
        return response;
    }
}
