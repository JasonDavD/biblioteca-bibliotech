package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Controlador para reportes del sistema.
 * Solo accesible por usuarios con rol ADMIN.
 */
@Controller
@RequestMapping("/reportes")
public class ReporteController {
    
    private final AuthService authService;
    private final PrestamoService prestamoService;
    private final ClienteService clienteService;
    private final LibroService libroService;
    private final AutorService autorService;
    private final CategoriaService categoriaService;
    
    public ReporteController(AuthService authService,
                             PrestamoService prestamoService,
                             ClienteService clienteService,
                             LibroService libroService,
                             AutorService autorService,
                             CategoriaService categoriaService) {
        this.authService = authService;
        this.prestamoService = prestamoService;
        this.clienteService = clienteService;
        this.libroService = libroService;
        this.autorService = autorService;
        this.categoriaService = categoriaService;
    }
    
    /**
     * Página principal de reportes.
     */
    @GetMapping
    public String index(Model model, HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        // Estadísticas generales
        model.addAttribute("totalLibros", libroService.contarTodos());
        model.addAttribute("totalEjemplares", libroService.contarTotalEjemplares());
        model.addAttribute("ejemplaresDisponibles", libroService.contarEjemplaresDisponibles());
        model.addAttribute("totalClientes", clienteService.contarTodos());
        model.addAttribute("clientesActivos", clienteService.contarActivos());
        model.addAttribute("totalAutores", autorService.contarTodos());
        model.addAttribute("totalCategorias", categoriaService.contarTodas());
        model.addAttribute("prestamosActivos", prestamoService.contarActivos());
        model.addAttribute("prestamosVencidos", prestamoService.contarVencidos());
        
        return "reportes/index";
    }
    
    /**
     * Reporte de préstamos vencidos.
     */
    @GetMapping("/prestamos-vencidos")
    public String prestamosVencidos(Model model, HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        // Actualizar estados
        prestamoService.actualizarPrestamosVencidos();
        
        List<PrestamoResponse> prestamosVencidos = prestamoService.listarVencidosConDetalles();
        
        model.addAttribute("prestamos", prestamosVencidos);
        model.addAttribute("totalVencidos", prestamosVencidos.size());
        model.addAttribute("titulo", "Reporte de Préstamos Vencidos");
        
        return "reportes/prestamos-vencidos";
    }
    
    /**
     * Reporte de clientes con préstamos vencidos.
     */
    @GetMapping("/clientes-morosos")
    public String clientesMorosos(Model model, HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        List<ClienteResponse> clientesMorosos = clienteService.listarConPrestamosVencidos();
        
        model.addAttribute("clientes", clientesMorosos);
        model.addAttribute("totalMorosos", clientesMorosos.size());
        model.addAttribute("titulo", "Reporte de Clientes Morosos");
        
        return "reportes/clientes-morosos";
    }
    
    /**
     * Reporte de estadísticas generales.
     */
    @GetMapping("/estadisticas")
    public String estadisticas(Model model, HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        // Estadísticas de biblioteca
        model.addAttribute("totalLibros", libroService.contarTodos());
        model.addAttribute("totalEjemplares", libroService.contarTotalEjemplares());
        model.addAttribute("ejemplaresDisponibles", libroService.contarEjemplaresDisponibles());
        long ejemplaresPrestados = libroService.contarTotalEjemplares() - libroService.contarEjemplaresDisponibles();
        model.addAttribute("ejemplaresPrestados", ejemplaresPrestados);
        
        // Estadísticas de clientes
        model.addAttribute("totalClientes", clienteService.contarTodos());
        model.addAttribute("clientesActivos", clienteService.contarActivos());
        model.addAttribute("clientesInactivos", clienteService.contarInactivos());
        
        // Estadísticas de préstamos
        model.addAttribute("prestamosActivos", prestamoService.contarActivos());
        model.addAttribute("prestamosVencidos", prestamoService.contarVencidos());
        model.addAttribute("prestamosHoy", prestamoService.contarPrestamosHoy());
        model.addAttribute("devolucionesHoy", prestamoService.contarDevolucionesHoy());
        
        // Estadísticas de catálogo
        model.addAttribute("totalAutores", autorService.contarTodos());
        model.addAttribute("totalCategorias", categoriaService.contarTodas());
        
        model.addAttribute("titulo", "Estadísticas del Sistema");
        
        return "reportes/estadisticas";
    }
}
