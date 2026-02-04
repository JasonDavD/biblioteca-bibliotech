package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.request.LibroRequest;
import com.biblioteca.bibliotech.dto.response.LibroResponse;
import com.biblioteca.bibliotech.entity.Autor;
import com.biblioteca.bibliotech.entity.Categoria;
import com.biblioteca.bibliotech.entity.Libro;
import com.biblioteca.bibliotech.exception.BusinessException;
import com.biblioteca.bibliotech.exception.ResourceNotFoundException;
import com.biblioteca.bibliotech.mapper.LibroMapper;
import com.biblioteca.bibliotech.repository.AutorRepository;
import com.biblioteca.bibliotech.repository.CategoriaRepository;
import com.biblioteca.bibliotech.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio para gestión de libros.
 * Incluye control de stock y validaciones.
 * Accesible por ADMIN y EMPLEADO.
 */
@Service
@Transactional
public class LibroService {
    
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final LibroMapper libroMapper;
    
    public LibroService(LibroRepository libroRepository,
                        AutorRepository autorRepository,
                        CategoriaRepository categoriaRepository,
                        LibroMapper libroMapper) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.categoriaRepository = categoriaRepository;
        this.libroMapper = libroMapper;
    }
    
    /**
     * Obtiene todos los libros.
     */
    @Transactional(readOnly = true)
    public List<LibroResponse> listarTodos() {
        List<Libro> libros = libroRepository.findAllByOrderByTituloAsc();
        return libroMapper.toResponseList(libros);
    }
    
    /**
     * Obtiene libros con disponibilidad.
     */
    @Transactional(readOnly = true)
    public List<LibroResponse> listarDisponibles() {
        List<Libro> libros = libroRepository.findLibrosDisponibles();
        return libroMapper.toResponseList(libros);
    }
    
    /**
     * Obtiene libros sin stock.
     */
    @Transactional(readOnly = true)
    public List<LibroResponse> listarSinStock() {
        List<Libro> libros = libroRepository.findLibrosSinStock();
        return libroMapper.toResponseList(libros);
    }
    
    /**
     * Busca un libro por su ID.
     */
    @Transactional(readOnly = true)
    public LibroResponse buscarPorId(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));
        return libroMapper.toResponse(libro);
    }
    
    /**
     * Busca un libro por ISBN.
     */
    @Transactional(readOnly = true)
    public LibroResponse buscarPorIsbn(String isbn) {
        Libro libro = libroRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "ISBN", isbn));
        return libroMapper.toResponse(libro);
    }
    
    /**
     * Busca libros por título (búsqueda parcial).
     */
    @Transactional(readOnly = true)
    public List<LibroResponse> buscarPorTitulo(String titulo) {
        List<Libro> libros = libroRepository.findByTituloContainingIgnoreCase(titulo);
        return libroMapper.toResponseList(libros);
    }
    
    /**
     * Busca libros por término (título, autor o ISBN).
     */
    @Transactional(readOnly = true)
    public List<LibroResponse> buscarPorTermino(String termino) {
        List<Libro> libros = libroRepository.buscarPorTermino(termino);
        return libroMapper.toResponseList(libros);
    }
    
    /**
     * Busca libros por autor.
     */
    @Transactional(readOnly = true)
    public List<LibroResponse> buscarPorAutor(Long autorId) {
        List<Libro> libros = libroRepository.findByAutorId(autorId);
        return libroMapper.toResponseList(libros);
    }
    
    /**
     * Busca libros por categoría.
     */
    @Transactional(readOnly = true)
    public List<LibroResponse> buscarPorCategoria(Long categoriaId) {
        List<Libro> libros = libroRepository.findByCategoriaId(categoriaId);
        return libroMapper.toResponseList(libros);
    }
    
    /**
     * Crea un nuevo libro.
     */
    public LibroResponse crear(LibroRequest request) {
        // Validar que el ISBN no exista
        if (libroRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un libro con el ISBN: " + request.getIsbn()
            );
        }
        
        // Obtener autor y categoría
        Autor autor = autorRepository.findById(request.getIdAutor())
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", request.getIdAutor()));
        
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getIdCategoria()));
        
        // Crear libro
        Libro libro = libroMapper.toEntity(request);
        libro.setAutor(autor);
        libro.setCategoria(categoria);
        libro.setCantidadDisponible(request.getCantidadTotal()); // Al crear, todo está disponible
        
        libro = libroRepository.save(libro);
        
        return libroMapper.toResponse(libro);
    }
    
    /**
     * Actualiza un libro existente.
     */
    public LibroResponse actualizar(Long id, LibroRequest request) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));
        
        // Validar ISBN único (si cambió)
        if (!libro.getIsbn().equals(request.getIsbn()) 
                && libroRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un libro con el ISBN: " + request.getIsbn()
            );
        }
        
        // Obtener autor y categoría
        Autor autor = autorRepository.findById(request.getIdAutor())
                .orElseThrow(() -> new ResourceNotFoundException("Autor", "id", request.getIdAutor()));
        
        Categoria categoria = categoriaRepository.findById(request.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", request.getIdCategoria()));
        
        // Calcular diferencia de stock
        int diferencia = request.getCantidadTotal() - libro.getCantidadTotal();
        int nuevaCantidadDisponible = libro.getCantidadDisponible() + diferencia;
        
        // Validar que no quede stock negativo
        if (nuevaCantidadDisponible < 0) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "No se puede reducir el stock total porque hay " + 
                    (libro.getCantidadTotal() - libro.getCantidadDisponible()) + 
                    " ejemplar(es) prestado(s)"
            );
        }
        
        // Actualizar libro
        libroMapper.updateEntityFromRequest(request, libro);
        libro.setAutor(autor);
        libro.setCategoria(categoria);
        libro.setCantidadDisponible(nuevaCantidadDisponible);
        
        libro = libroRepository.save(libro);
        
        return libroMapper.toResponse(libro);
    }
    
    /**
     * Elimina un libro.
     */
    public void eliminar(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));
        
        // Verificar que no tenga préstamos activos
        int prestados = libro.getCantidadTotal() - libro.getCantidadDisponible();
        if (prestados > 0) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "No se puede eliminar el libro porque tiene " + prestados + " ejemplar(es) prestado(s)"
            );
        }
        
        // Verificar que no tenga historial de préstamos
        if (!libro.getPrestamos().isEmpty()) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "No se puede eliminar el libro porque tiene historial de préstamos"
            );
        }
        
        libroRepository.delete(libro);
    }
    
    /**
     * Verifica si hay stock disponible.
     */
    @Transactional(readOnly = true)
    public boolean tieneDisponibilidad(Long libroId) {
        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", libroId));
        return libro.getCantidadDisponible() > 0;
    }
    
    /**
     * Reduce el stock disponible (usado al prestar).
     * Retorna true si se pudo reducir.
     */
    public boolean reducirStock(Long libroId) {
        int filasAfectadas = libroRepository.reducirStock(libroId);
        return filasAfectadas > 0;
    }
    
    /**
     * Aumenta el stock disponible (usado al devolver).
     * Retorna true si se pudo aumentar.
     */
    public boolean aumentarStock(Long libroId) {
        int filasAfectadas = libroRepository.aumentarStock(libroId);
        return filasAfectadas > 0;
    }
    
    /**
     * Cuenta el total de libros (títulos únicos).
     */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return libroRepository.count();
    }
    
    /**
     * Cuenta el total de ejemplares en la biblioteca.
     */
    @Transactional(readOnly = true)
    public long contarTotalEjemplares() {
        return libroRepository.countTotalEjemplares();
    }
    
    /**
     * Cuenta el total de ejemplares disponibles.
     */
    @Transactional(readOnly = true)
    public long contarEjemplaresDisponibles() {
        return libroRepository.countEjemplaresDisponibles();
    }
    
    /**
     * Obtiene la entidad Libro para uso interno.
     */
    @Transactional(readOnly = true)
    public Libro obtenerEntidadPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", id));
    }
}
