package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.request.ClienteRequest;
import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.entity.Cliente;
import com.biblioteca.bibliotech.exception.BusinessException;
import com.biblioteca.bibliotech.exception.ResourceNotFoundException;
import com.biblioteca.bibliotech.mapper.ClienteMapper;
import com.biblioteca.bibliotech.repository.ClienteRepository;
import com.biblioteca.bibliotech.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de clientes (socios/lectores).
 * Accesible por ADMIN y EMPLEADO.
 */
@Service
@Transactional
public class ClienteService {
    
    private static final int LIMITE_PRESTAMOS = 3;
    
    private final ClienteRepository clienteRepository;
    private final PrestamoRepository prestamoRepository;
    private final ClienteMapper clienteMapper;
    
    public ClienteService(ClienteRepository clienteRepository,
                          PrestamoRepository prestamoRepository,
                          ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.prestamoRepository = prestamoRepository;
        this.clienteMapper = clienteMapper;
    }
    
    /**
     * Obtiene todos los clientes.
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        List<Cliente> clientes = clienteRepository.findAllByOrderByApellidoAscNombreAsc();
        return clientes.stream()
                .map(this::convertirConEstadisticas)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene solo clientes activos.
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarActivos() {
        List<Cliente> clientes = clienteRepository.findByActivoTrue();
        return clientes.stream()
                .map(this::convertirConEstadisticas)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene solo clientes inactivos.
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarInactivos() {
        List<Cliente> clientes = clienteRepository.findByActivoFalse();
        return clientes.stream()
                .map(this::convertirConEstadisticas)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca un cliente por su ID.
     */
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        return convertirConEstadisticas(cliente);
    }
    
    /**
     * Busca un cliente por su DNI.
     */
    @Transactional(readOnly = true)
    public ClienteResponse buscarPorDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "DNI", dni));
        return convertirConEstadisticas(cliente);
    }
    
    /**
     * Busca clientes por término (nombre, apellido o DNI).
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarPorTermino(String termino) {
        List<Cliente> clientes = clienteRepository.buscarPorTermino(termino);
        return clientes.stream()
                .map(this::convertirConEstadisticas)
                .collect(Collectors.toList());
    }
    
    /**
     * Busca clientes activos por término.
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> buscarActivosPorTermino(String termino) {
        List<Cliente> clientes = clienteRepository.buscarActivosPorTermino(termino);
        return clientes.stream()
                .map(this::convertirConEstadisticas)
                .collect(Collectors.toList());
    }
    
    /**
     * Crea un nuevo cliente.
     */
    public ClienteResponse crear(ClienteRequest request) {
        // Validar que el DNI no exista
        if (clienteRepository.existsByDni(request.getDni())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un cliente con el DNI: " + request.getDni()
            );
        }
        
        // Validar que el email no exista (si se proporciona)
        if (request.getEmail() != null && !request.getEmail().isEmpty() 
                && clienteRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un cliente con el email: " + request.getEmail()
            );
        }
        
        Cliente cliente = clienteMapper.toEntity(request);
        cliente = clienteRepository.save(cliente);
        
        return convertirConEstadisticas(cliente);
    }
    
    /**
     * Actualiza un cliente existente.
     */
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        
        // Validar DNI único (si cambió)
        if (!cliente.getDni().equals(request.getDni()) 
                && clienteRepository.existsByDni(request.getDni())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un cliente con el DNI: " + request.getDni()
            );
        }
        
        // Validar email único (si cambió y se proporciona)
        if (request.getEmail() != null && !request.getEmail().isEmpty()
                && !request.getEmail().equals(cliente.getEmail())
                && clienteRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    BusinessException.DUPLICADO,
                    "Ya existe un cliente con el email: " + request.getEmail()
            );
        }
        
        clienteMapper.updateEntityFromRequest(request, cliente);
        cliente = clienteRepository.save(cliente);
        
        return convertirConEstadisticas(cliente);
    }
    
    /**
     * Activa un cliente.
     */
    public ClienteResponse activar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        
        cliente.setActivo(true);
        cliente = clienteRepository.save(cliente);
        
        return convertirConEstadisticas(cliente);
    }
    
    /**
     * Desactiva un cliente.
     */
    public ClienteResponse desactivar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        
        // Verificar que no tenga préstamos activos
        Long prestamosActivos = prestamoRepository.countPrestamosActivosByClienteId(id);
        if (prestamosActivos > 0) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "No se puede desactivar el cliente porque tiene " + prestamosActivos + " préstamo(s) activo(s)"
            );
        }
        
        cliente.setActivo(false);
        cliente = clienteRepository.save(cliente);
        
        return convertirConEstadisticas(cliente);
    }
    
    /**
     * Elimina un cliente.
     */
    public void eliminar(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
        
        // Verificar que no tenga préstamos (histórico)
        if (!cliente.getPrestamos().isEmpty()) {
            throw new BusinessException(
                    BusinessException.OPERACION_NO_PERMITIDA,
                    "No se puede eliminar el cliente porque tiene historial de préstamos. " +
                    "Considere desactivarlo en su lugar."
            );
        }
        
        clienteRepository.delete(cliente);
    }
    
    /**
     * Verifica si el cliente puede realizar préstamos.
     */
    @Transactional(readOnly = true)
    public boolean puedeRealizarPrestamo(Long clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clienteId));
        
        if (!cliente.getActivo()) {
            return false;
        }
        
        Long prestamosActivos = prestamoRepository.countPrestamosActivosByClienteId(clienteId);
        return prestamosActivos < LIMITE_PRESTAMOS;
    }
    
    /**
     * Obtiene clientes con préstamos vencidos.
     */
    @Transactional(readOnly = true)
    public List<ClienteResponse> listarConPrestamosVencidos() {
        List<Cliente> clientes = clienteRepository.findClientesConPrestamosVencidos();
        return clientes.stream()
                .map(this::convertirConEstadisticas)
                .collect(Collectors.toList());
    }
    
    /**
     * Cuenta el total de clientes.
     */
    @Transactional(readOnly = true)
    public long contarTodos() {
        return clienteRepository.count();
    }
    
    /**
     * Cuenta clientes activos.
     */
    @Transactional(readOnly = true)
    public long contarActivos() {
        return clienteRepository.countByActivoTrue();
    }
    
    /**
     * Cuenta clientes inactivos.
     */
    @Transactional(readOnly = true)
    public long contarInactivos() {
        return clienteRepository.countByActivoFalse();
    }
    
    /**
     * Obtiene la entidad Cliente para uso interno.
     */
    @Transactional(readOnly = true)
    public Cliente obtenerEntidadPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
    }
    
    /**
     * Convierte una entidad Cliente a Response incluyendo estadísticas de préstamos.
     */
    private ClienteResponse convertirConEstadisticas(Cliente cliente) {
        ClienteResponse response = clienteMapper.toResponse(cliente);
        
        // Calcular estadísticas de préstamos
        Long prestamosActivos = prestamoRepository.countPrestamosActivosByClienteId(cliente.getId());
        Long totalPrestamos = (long) cliente.getPrestamos().size();
        
        // Contar préstamos vencidos
        long prestamosVencidos = cliente.getPrestamos().stream()
                .filter(p -> p.estaVencido())
                .count();
        
        response.setPrestamosActivos(prestamosActivos);
        response.setPrestamosVencidos(prestamosVencidos);
        response.setTotalPrestamos(totalPrestamos);
        response.setPuedePrestar(cliente.getActivo() && prestamosActivos < LIMITE_PRESTAMOS);
        
        return response;
    }
}
