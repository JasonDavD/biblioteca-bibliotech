package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Controlador para el Dashboard principal.
 * Muestra estadísticas y resumen del sistema.
 */
@Controller
public class DashboardController {
    
    private final AuthService authService;
    private final LibroService libroService;
    private final ClienteService clienteService;
    private final PrestamoService prestamoService;
    private final AutorService autorService;
    private final CategoriaService categoriaService;
    
    public DashboardController(AuthService authService,
                                LibroService libroService,
                                ClienteService clienteService,
                                PrestamoService prestamoService,
                                AutorService autorService,
                                CategoriaService categoriaService) {
        this.authService = authService;
        this.libroService = libroService;
        this.clienteService = clienteService;
        this.prestamoService = prestamoService;
        this.autorService = autorService;
        this.categoriaService = categoriaService;
    }
    
    /**
     * Muestra el dashboard con estadísticas generales.
     */
    @GetMapping("/dashboard")
    public String mostrarDashboard(Model model, HttpSession session) {
        // Verificar sesión
        authService.verificarSesionActiva(session);
        
        // Actualizar préstamos vencidos
        prestamoService.actualizarPrestamosVencidos();
        
        // Estadísticas de libros
        model.addAttribute("totalLibros", libroService.contarTodos());
        model.addAttribute("totalEjemplares", libroService.contarTotalEjemplares());
        model.addAttribute("ejemplaresDisponibles", libroService.contarEjemplaresDisponibles());
        
        // Estadísticas de clientes
        model.addAttribute("totalClientes", clienteService.contarTodos());
        model.addAttribute("clientesActivos", clienteService.contarActivos());
        
        // Estadísticas de préstamos
        model.addAttribute("prestamosActivos", prestamoService.contarActivos());
        model.addAttribute("prestamosVencidos", prestamoService.contarVencidos());
        model.addAttribute("prestamosHoy", prestamoService.contarPrestamosHoy());
        model.addAttribute("devolucionesHoy", prestamoService.contarDevolucionesHoy());
        
        // Estadísticas adicionales
        model.addAttribute("totalAutores", autorService.contarTodos());
        model.addAttribute("totalCategorias", categoriaService.contarTodas());
        
        // Préstamos por vencer (próximos 3 días)
        List<PrestamoResponse> prestamosPorVencer = prestamoService.listarPorVencer();
        model.addAttribute("prestamosPorVencer", prestamosPorVencer);
        
        // Préstamos vencidos (para alertas)
        List<PrestamoResponse> prestamosVencidosList = prestamoService.listarVencidos();
        model.addAttribute("prestamosVencidosList", prestamosVencidosList);
        
        return "dashboard";
    }
}
