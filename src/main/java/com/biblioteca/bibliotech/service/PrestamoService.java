package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.request.PrestamoRequest;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.entity.Cliente;
import com.biblioteca.bibliotech.entity.Libro;
import com.biblioteca.bibliotech.entity.Prestamo;
import com.biblioteca.bibliotech.enums.EstadoPrestamo;
import com.biblioteca.bibliotech.exception.BusinessException;
import com.biblioteca.bibliotech.exception.ResourceNotFoundException;
import com.biblioteca.bibliotech.mapper.PrestamoMapper;
import com.biblioteca.bibliotech.repository.ClienteRepository;
import com.biblioteca.bibliotech.repository.LibroRepository;
import com.biblioteca.bibliotech.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio para gestión de préstamos.
 * Implementa la lógica transaccional crítica del sistema.
 * Accesible por ADMIN y EMPLEADO.
 */
@Service
@Transactional
public class PrestamoService {
    
    private static final int LIMITE_PRESTAMOS_POR_CLIENTE = 3;
    private static final int DIAS_PRESTAMO_DEFAULT = 14;
    
    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final ClienteRepository clienteRepository;
    private final PrestamoMapper prestamoMapper;
    
    public PrestamoService(PrestamoRepository prestamoRepository,
                           LibroRepository libroRepository,
                           ClienteRepository clienteRepository,
                           PrestamoMapper prestamoMapper) {
        this.prestamoRepository = prestamoRepository;
        this.libroRepository = libroRepository;
        this.clienteRepository = clienteRepository;
        this.prestamoMapper = prestamoMapper;
    }
    
    // ==================== OPERACIONES DE CONSULTA ====================
    
    /**
     * Obtiene todos los préstamos.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarTodos() {
        List<Prestamo> prestamos = prestamoRepository.findPrestamosRecientes();
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene préstamos activos.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarActivos() {
        List<Prestamo> prestamos = prestamoRepository.findPrestamosActivos();
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene préstamos vencidos.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarVencidos() {
        List<Prestamo> prestamos = prestamoRepository.findPrestamosVencidos();
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene préstamos por estado.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarPorEstado(EstadoPrestamo estado) {
        List<Prestamo> prestamos = prestamoRepository.findByEstado(estado);
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene préstamos que vencen pronto (próximos 3 días).
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarPorVencer() {
        LocalDate fechaLimite = LocalDate.now().plusDays(3);
        List<Prestamo> prestamos = prestamoRepository.findPrestamosPorVencer(fechaLimite);
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Busca un préstamo por su ID.
     */
    @Transactional(readOnly = true)
    public PrestamoResponse buscarPorId(Long id) {
        Prestamo prestamo = prestamoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo", "id", id));
        return prestamoMapper.toResponse(prestamo);
    }
    
    /**
     * Obtiene préstamos de un cliente.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> buscarPorCliente(Long clienteId) {
        List<Prestamo> prestamos = prestamoRepository.findByClienteId(clienteId);
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene préstamos activos de un cliente.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> buscarActivosPorCliente(Long clienteId) {
        List<Prestamo> prestamos = prestamoRepository.findPrestamosActivosByClienteId(clienteId);
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene historial de préstamos de un cliente.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> obtenerHistorialCliente(Long clienteId) {
        List<Prestamo> prestamos = prestamoRepository.findHistorialByClienteId(clienteId);
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene préstamos de un libro.
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> buscarPorLibro(Long libroId) {
        List<Prestamo> prestamos = prestamoRepository.findByLibroId(libroId);
        return prestamoMapper.toResponseList(prestamos);
    }
    
    /**
     * Obtiene préstamos vencidos con detalles completos (para reportes).
     */
    @Transactional(readOnly = true)
    public List<PrestamoResponse> listarVencidosConDetalles() {
        List<Prestamo> prestamos = prestamoRepository.findPrestamosVencidosConDetalles();
        return prestamoMapper.toResponseList(prestamos);
    }
    
    // ==================== OPERACIONES TRANSACCIONALES ====================
    
    /**
     * Crea un nuevo préstamo.
     * Implementa todas las validaciones de negocio.
     * 
     * @param request Datos del préstamo
     * @return PrestamoResponse con el préstamo creado
     * @throws BusinessException si no se cumplen las reglas de negocio
     */
    public PrestamoResponse crear(PrestamoRequest request) {
        // 1. Obtener entidades
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getIdCliente()));
        
        Libro libro = libroRepository.findById(request.getIdLibro())
                .orElseThrow(() -> new ResourceNotFoundException("Libro", "id", request.getIdLibro()));
        
        // 2. VALIDACIÓN: Cliente debe estar activo
        if (!cliente.getActivo()) {
            throw new BusinessException(
                    BusinessException.CLIENTE_INACTIVO,
                    "El cliente " + cliente.getNombreCompleto() + " está inactivo y no puede realizar préstamos"
            );
        }
        
        // 3. VALIDACIÓN: Verificar límite de 3 préstamos por cliente
        Long prestamosActivos = prestamoRepository.countPrestamosActivosByClienteId(cliente.getId());
        if (prestamosActivos >= LIMITE_PRESTAMOS_POR_CLIENTE) {
            throw new BusinessException(
                    BusinessException.LIMITE_PRESTAMOS,
                    "El cliente " + cliente.getNombreCompleto() + " ya tiene " + 
                    prestamosActivos + " préstamos activos. El límite es " + LIMITE_PRESTAMOS_POR_CLIENTE
            );
        }
        
        // 4. VALIDACIÓN: Verificar que el cliente no tenga ya este libro prestado
        if (prestamoRepository.existsPrestamoActivoByClienteIdAndLibroId(cliente.getId(), libro.getId())) {
            throw new BusinessException(
                    BusinessException.LIBRO_YA_PRESTADO,
                    "El cliente " + cliente.getNombreCompleto() + " ya tiene prestado el libro '" + libro.getTitulo() + "'"
            );
        }
        
        // 5. VALIDACIÓN: Verificar stock disponible
        if (!libro.tieneDisponibilidad()) {
            throw new BusinessException(
                    BusinessException.SIN_STOCK,
                    "El libro '" + libro.getTitulo() + "' no tiene ejemplares disponibles"
            );
        }
        
        // 6. Crear préstamo
        Prestamo prestamo = new Prestamo();
        prestamo.setCliente(cliente);
        prestamo.setLibro(libro);
        prestamo.setFechaPrestamo(LocalDate.now());
        prestamo.setFechaDevolucionEsperada(request.getFechaDevolucionEsperada());
        prestamo.setObservaciones(request.getObservaciones());
        prestamo.setEstado(EstadoPrestamo.ACTIVO);
        
        // 7. Reducir stock del libro
        libro.reducirStock();
        libroRepository.save(libro);
        
        // 8. Guardar préstamo
        prestamo = prestamoRepository.save(prestamo);
        
        return prestamoMapper.toResponse(prestamo);
    }
    
    /**
     * Crea un préstamo con fecha de devolución por defecto (14 días).
     */
    public PrestamoResponse crearConFechaDefault(Long idLibro, Long idCliente, String observaciones) {
        PrestamoRequest request = PrestamoRequest.builder()
                .idLibro(idLibro)
                .idCliente(idCliente)
                .fechaDevolucionEsperada(LocalDate.now().plusDays(DIAS_PRESTAMO_DEFAULT))
                .observaciones(observaciones)
                .build();
        
        return crear(request);
    }
    
    /**
     * Procesa la devolución de un préstamo.
     * 
     * @param prestamoId ID del préstamo a devolver
     * @param observaciones Observaciones adicionales (opcional)
     * @return PrestamoResponse con el préstamo actualizado
     */
    public PrestamoResponse devolver(Long prestamoId, String observaciones) {
        // 1. Obtener préstamo
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo", "id", prestamoId));
        
        // 2. VALIDACIÓN: Verificar que esté activo o vencido (no ya devuelto)
        if (EstadoPrestamo.DEVUELTO.equals(prestamo.getEstado())) {
            throw new BusinessException(
                    BusinessException.PRESTAMO_NO_ACTIVO,
                    "El préstamo ya fue devuelto el " + prestamo.getFechaDevolucionReal()
            );
        }
        
        // 3. Marcar como devuelto
        prestamo.marcarComoDevuelto();
        
        // 4. Agregar observaciones si las hay
        if (observaciones != null && !observaciones.isEmpty()) {
            String obsActuales = prestamo.getObservaciones();
            if (obsActuales != null && !obsActuales.isEmpty()) {
                prestamo.setObservaciones(obsActuales + " | Devolución: " + observaciones);
            } else {
                prestamo.setObservaciones("Devolución: " + observaciones);
            }
        }
        
        // 5. Aumentar stock del libro
        Libro libro = prestamo.getLibro();
        libro.aumentarStock();
        libroRepository.save(libro);
        
        // 6. Guardar préstamo
        prestamo = prestamoRepository.save(prestamo);
        
        return prestamoMapper.toResponse(prestamo);
    }
    
    /**
     * Actualiza los estados de préstamos vencidos.
     * Debe ejecutarse periódicamente (ej: al iniciar la aplicación o mediante scheduler).
     * 
     * @return Cantidad de préstamos actualizados
     */
    public int actualizarPrestamosVencidos() {
        return prestamoRepository.actualizarPrestamosVencidos();
    }
    
    /**
     * Extiende la fecha de devolución de un préstamo.
     * 
     * @param prestamoId ID del préstamo
     * @param nuevaFecha Nueva fecha de devolución
     * @return PrestamoResponse actualizado
     */
    public PrestamoResponse extenderPlazo(Long prestamoId, LocalDate nuevaFecha) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo", "id", prestamoId));
        
        // Validar que esté activo
        if (!EstadoPrestamo.ACTIVO.equals(prestamo.getEstado()) && 
            !EstadoPrestamo.VENCIDO.equals(prestamo.getEstado())) {
            throw new BusinessException(
                    BusinessException.PRESTAMO_NO_ACTIVO,
                    "Solo se puede extender préstamos activos o vencidos"
            );
        }
        
        // Validar que la nueva fecha sea posterior a hoy
        if (!nuevaFecha.isAfter(LocalDate.now())) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "La nueva fecha debe ser posterior a hoy"
            );
        }
        
        prestamo.setFechaDevolucionEsperada(nuevaFecha);
        
        // Si estaba vencido, volver a activo
        if (EstadoPrestamo.VENCIDO.equals(prestamo.getEstado())) {
            prestamo.setEstado(EstadoPrestamo.ACTIVO);
        }
        
        String obsActuales = prestamo.getObservaciones();
        String nuevaObs = "Plazo extendido hasta " + nuevaFecha;
        if (obsActuales != null && !obsActuales.isEmpty()) {
            prestamo.setObservaciones(obsActuales + " | " + nuevaObs);
        } else {
            prestamo.setObservaciones(nuevaObs);
        }
        
        prestamo = prestamoRepository.save(prestamo);
        
        return prestamoMapper.toResponse(prestamo);
    }
    
    // ==================== ESTADÍSTICAS ====================
    
    /**
     * Cuenta préstamos por estado.
     */
    @Transactional(readOnly = true)
    public long contarPorEstado(EstadoPrestamo estado) {
        return prestamoRepository.countByEstado(estado);
    }
    
    /**
     * Cuenta préstamos activos totales.
     */
    @Transactional(readOnly = true)
    public long contarActivos() {
        return prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO);
    }
    
    /**
     * Cuenta préstamos vencidos totales.
     */
    @Transactional(readOnly = true)
    public long contarVencidos() {
        return prestamoRepository.findPrestamosVencidos().size();
    }
    
    /**
     * Cuenta préstamos realizados hoy.
     */
    @Transactional(readOnly = true)
    public long contarPrestamosHoy() {
        return prestamoRepository.countPrestamosHoy();
    }
    
    /**
     * Cuenta devoluciones realizadas hoy.
     */
    @Transactional(readOnly = true)
    public long contarDevolucionesHoy() {
        return prestamoRepository.countDevolucionesHoy();
    }
    
    /**
     * Cuenta préstamos activos de un cliente.
     */
    @Transactional(readOnly = true)
    public long contarActivosPorCliente(Long clienteId) {
        return prestamoRepository.countPrestamosActivosByClienteId(clienteId);
    }
}
