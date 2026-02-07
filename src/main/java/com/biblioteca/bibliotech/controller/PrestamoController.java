package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.request.PrestamoRequest;
import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.dto.response.LibroResponse;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.enums.EstadoPrestamo;
import com.biblioteca.bibliotech.service.AuthService;
import com.biblioteca.bibliotech.service.ClienteService;
import com.biblioteca.bibliotech.service.LibroService;
import com.biblioteca.bibliotech.service.PrestamoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para gestión de préstamos.
 * Maneja el registro de préstamos, devoluciones y consultas.
 * Accesible por ADMIN y EMPLEADO.
 */
@Controller
@RequestMapping("/prestamos")
public class PrestamoController {
    
    private final PrestamoService prestamoService;
    private final LibroService libroService;
    private final ClienteService clienteService;
    private final AuthService authService;
    
    public PrestamoController(PrestamoService prestamoService,
                              LibroService libroService,
                              ClienteService clienteService,
                              AuthService authService) {
        this.prestamoService = prestamoService;
        this.libroService = libroService;
        this.clienteService = clienteService;
        this.authService = authService;
    }
    
    /**
     * Lista todos los préstamos con filtros.
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String filtro,
                         @RequestParam(required = false) Long clienteId,
                         @RequestParam(required = false) Long libroId,
                         Model model, 
                         HttpSession session) {
        
        authService.verificarSesionActiva(session);
        
        // Actualizar estados vencidos
        prestamoService.actualizarPrestamosVencidos();
        
        List<PrestamoResponse> prestamos;
        
        // Aplicar filtros
        if (clienteId != null) {
            prestamos = prestamoService.buscarPorCliente(clienteId);
            model.addAttribute("clienteIdSeleccionado", clienteId);
        } else if (libroId != null) {
            prestamos = prestamoService.buscarPorLibro(libroId);
            model.addAttribute("libroIdSeleccionado", libroId);
        } else if ("activos".equals(filtro)) {
            prestamos = prestamoService.listarActivos();
        } else if ("vencidos".equals(filtro)) {
            prestamos = prestamoService.listarVencidos();
        } else if ("porVencer".equals(filtro)) {
            prestamos = prestamoService.listarPorVencer();
        } else if ("devueltos".equals(filtro)) {
            prestamos = prestamoService.listarPorEstado(EstadoPrestamo.DEVUELTO);
        } else {
            prestamos = prestamoService.listarTodos();
        }
        
        // Estadísticas
        model.addAttribute("prestamos", prestamos);
        model.addAttribute("filtroActual", filtro);
        model.addAttribute("totalActivos", prestamoService.contarActivos());
        model.addAttribute("totalVencidos", prestamoService.contarVencidos());
        model.addAttribute("prestamosHoy", prestamoService.contarPrestamosHoy());
        model.addAttribute("devolucionesHoy", prestamoService.contarDevolucionesHoy());
        
        return "prestamos/lista";
    }
    
    /**
     * Muestra el formulario para crear un nuevo préstamo.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(@RequestParam(required = false) Long clienteId,
                                          @RequestParam(required = false) Long libroId,
                                          Model model, 
                                          HttpSession session) {
        authService.verificarSesionActiva(session);
        
        // Listas para los selects
        List<ClienteResponse> clientes = clienteService.listarActivos();
        List<LibroResponse> libros = libroService.listarDisponibles();
        
        PrestamoRequest request = new PrestamoRequest();
        // Fecha de devolución por defecto: 14 días
        request.setFechaDevolucionEsperada(LocalDate.now().plusDays(14));
        
        // Pre-seleccionar si vienen parámetros
        if (clienteId != null) {
            request.setIdCliente(clienteId);
        }
        if (libroId != null) {
            request.setIdLibro(libroId);
        }
        
        model.addAttribute("prestamoRequest", request);
        model.addAttribute("clientes", clientes);
        model.addAttribute("libros", libros);
        model.addAttribute("fechaHoy", LocalDate.now());
        model.addAttribute("fechaMinima", LocalDate.now().plusDays(1));
        
        return "prestamos/nuevo";
    }
    
    /**
     * Procesa la creación de un nuevo préstamo.
     */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("prestamoRequest") PrestamoRequest request,
                        BindingResult result,
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("clientes", clienteService.listarActivos());
            model.addAttribute("libros", libroService.listarDisponibles());
            model.addAttribute("fechaHoy", LocalDate.now());
            model.addAttribute("fechaMinima", LocalDate.now().plusDays(1));
            return "prestamos/nuevo";
        }
        
        try {
            PrestamoResponse prestamo = prestamoService.crear(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Préstamo registrado exitosamente. Libro: '" + prestamo.getTituloLibro() + 
                    "' - Cliente: " + prestamo.getNombreCompletoCliente());
            return "redirect:/prestamos";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("clientes", clienteService.listarActivos());
            model.addAttribute("libros", libroService.listarDisponibles());
            model.addAttribute("fechaHoy", LocalDate.now());
            model.addAttribute("fechaMinima", LocalDate.now().plusDays(1));
            return "prestamos/nuevo";
        }
    }
    
    /**
     * Muestra la pantalla de devolución de un préstamo.
     */
    @GetMapping("/devolver/{id}")
    public String mostrarDevolucion(@PathVariable Long id, 
                                     Model model, 
                                     HttpSession session) {
        authService.verificarSesionActiva(session);
        
        PrestamoResponse prestamo = prestamoService.buscarPorId(id);
        
        // Verificar que el préstamo esté activo o vencido
        if (EstadoPrestamo.DEVUELTO.equals(prestamo.getEstado())) {
            return "redirect:/prestamos?error=El préstamo ya fue devuelto";
        }
        
        model.addAttribute("prestamo", prestamo);
        model.addAttribute("fechaHoy", LocalDate.now());
        
        return "prestamos/devolucion";
    }
    
    /**
     * Procesa la devolución de un préstamo.
     */
    @PostMapping("/devolver/{id}")
    public String devolver(@PathVariable Long id,
                           @RequestParam(required = false) String observaciones,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            PrestamoResponse prestamo = prestamoService.devolver(id, observaciones);
            
            String mensaje = "Devolución registrada exitosamente. Libro: '" + prestamo.getTituloLibro() + "'";
            if (prestamo.getDiasRetraso() > 0) {
                mensaje += " (Devuelto con " + prestamo.getDiasRetraso() + " día(s) de retraso)";
            }
            
            redirectAttributes.addFlashAttribute("successMessage", mensaje);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/prestamos";
    }
    
    /**
     * Ver detalles de un préstamo.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        PrestamoResponse prestamo = prestamoService.buscarPorId(id);
        model.addAttribute("prestamo", prestamo);
        
        return "prestamos/ver";
    }
    
    /**
     * Muestra el formulario para extender el plazo de un préstamo.
     */
    @GetMapping("/extender/{id}")
    public String mostrarExtension(@PathVariable Long id, 
                                    Model model, 
                                    HttpSession session) {
        authService.verificarSesionActiva(session);
        
        PrestamoResponse prestamo = prestamoService.buscarPorId(id);
        
        // Verificar que el préstamo esté activo o vencido
        if (EstadoPrestamo.DEVUELTO.equals(prestamo.getEstado())) {
            return "redirect:/prestamos?error=No se puede extender un préstamo devuelto";
        }
        
        model.addAttribute("prestamo", prestamo);
        model.addAttribute("fechaMinima", LocalDate.now().plusDays(1));
        model.addAttribute("nuevaFecha", LocalDate.now().plusDays(14));
        
        return "prestamos/extender";
    }
    
    /**
     * Procesa la extensión del plazo de un préstamo.
     */
    @PostMapping("/extender/{id}")
    public String extender(@PathVariable Long id,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate nuevaFecha,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            PrestamoResponse prestamo = prestamoService.extenderPlazo(id, nuevaFecha);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Plazo extendido exitosamente hasta " + nuevaFecha + 
                    " para el libro '" + prestamo.getTituloLibro() + "'");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/prestamos";
    }
    
    /**
     * Lista préstamos vencidos (para reportes).
     */
    @GetMapping("/vencidos")
    public String listarVencidos(Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        // Actualizar estados
        prestamoService.actualizarPrestamosVencidos();
        
        List<PrestamoResponse> prestamosVencidos = prestamoService.listarVencidosConDetalles();
        
        model.addAttribute("prestamos", prestamosVencidos);
        model.addAttribute("totalVencidos", prestamosVencidos.size());
        
        return "prestamos/vencidos";
    }
    
    /**
     * Préstamo rápido desde la vista de libro.
     */
    @GetMapping("/rapido")
    public String prestamoRapido(@RequestParam Long libroId,
                                  Model model,
                                  HttpSession session) {
        authService.verificarSesionActiva(session);
        
        LibroResponse libro = libroService.buscarPorId(libroId);
        List<ClienteResponse> clientes = clienteService.listarActivos();
        
        PrestamoRequest request = new PrestamoRequest();
        request.setIdLibro(libroId);
        request.setFechaDevolucionEsperada(LocalDate.now().plusDays(14));
        
        model.addAttribute("prestamoRequest", request);
        model.addAttribute("libro", libro);
        model.addAttribute("clientes", clientes);
        model.addAttribute("fechaMinima", LocalDate.now().plusDays(1));
        model.addAttribute("esPrestamoRapido", true);
        
        return "prestamos/nuevo";
    }
}
