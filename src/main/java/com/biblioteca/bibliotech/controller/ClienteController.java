package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.request.ClienteRequest;
import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.service.AuthService;
import com.biblioteca.bibliotech.service.ClienteService;
import com.biblioteca.bibliotech.service.PrestamoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para gestión de clientes (socios/lectores).
 * Accesible por ADMIN y EMPLEADO.
 */
@Controller
@RequestMapping("/clientes")
public class ClienteController {
    
    private final ClienteService clienteService;
    private final PrestamoService prestamoService;
    private final AuthService authService;
    
    public ClienteController(ClienteService clienteService, 
                             PrestamoService prestamoService,
                             AuthService authService) {
        this.clienteService = clienteService;
        this.prestamoService = prestamoService;
        this.authService = authService;
    }
    
    /**
     * Lista todos los clientes.
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String filtro,
                         @RequestParam(required = false) String busqueda,
                         Model model, 
                         HttpSession session) {
        
        authService.verificarSesionActiva(session);
        
        List<ClienteResponse> clientes;
        
        // Aplicar filtros
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            clientes = clienteService.buscarPorTermino(busqueda.trim());
            model.addAttribute("busqueda", busqueda);
        } else if ("activos".equals(filtro)) {
            clientes = clienteService.listarActivos();
        } else if ("inactivos".equals(filtro)) {
            clientes = clienteService.listarInactivos();
        } else if ("vencidos".equals(filtro)) {
            clientes = clienteService.listarConPrestamosVencidos();
        } else {
            clientes = clienteService.listarTodos();
        }
        
        model.addAttribute("clientes", clientes);
        model.addAttribute("filtroActual", filtro);
        model.addAttribute("totalClientes", clienteService.contarTodos());
        model.addAttribute("clientesActivos", clienteService.contarActivos());
        model.addAttribute("clientesInactivos", clienteService.contarInactivos());
        
        return "clientes/lista";
    }
    
    /**
     * Muestra el formulario para crear un nuevo cliente.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        model.addAttribute("clienteRequest", new ClienteRequest());
        model.addAttribute("titulo", "Nuevo Cliente");
        model.addAttribute("esNuevo", true);
        
        return "clientes/formulario";
    }
    
    /**
     * Procesa la creación de un nuevo cliente.
     */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("clienteRequest") ClienteRequest request,
                        BindingResult result,
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nuevo Cliente");
            model.addAttribute("esNuevo", true);
            return "clientes/formulario";
        }
        
        try {
            ClienteResponse cliente = clienteService.crear(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cliente '" + cliente.getNombreCompleto() + "' registrado exitosamente");
            return "redirect:/clientes";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("titulo", "Nuevo Cliente");
            model.addAttribute("esNuevo", true);
            return "clientes/formulario";
        }
    }
    
    /**
     * Muestra el formulario para editar un cliente existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, 
                                           Model model, 
                                           HttpSession session) {
        authService.verificarSesionActiva(session);
        
        ClienteResponse cliente = clienteService.buscarPorId(id);
        
        // Convertir Response a Request para el formulario
        ClienteRequest request = new ClienteRequest();
        request.setDni(cliente.getDni());
        request.setNombre(cliente.getNombre());
        request.setApellido(cliente.getApellido());
        request.setEmail(cliente.getEmail());
        request.setTelefono(cliente.getTelefono());
        request.setDireccion(cliente.getDireccion());
        request.setActivo(cliente.getActivo());
        
        model.addAttribute("clienteRequest", request);
        model.addAttribute("clienteId", id);
        model.addAttribute("titulo", "Editar Cliente");
        model.addAttribute("esNuevo", false);
        
        return "clientes/formulario";
    }
    
    /**
     * Procesa la actualización de un cliente.
     */
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("clienteRequest") ClienteRequest request,
                             BindingResult result,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("clienteId", id);
            model.addAttribute("titulo", "Editar Cliente");
            model.addAttribute("esNuevo", false);
            return "clientes/formulario";
        }
        
        try {
            ClienteResponse cliente = clienteService.actualizar(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cliente '" + cliente.getNombreCompleto() + "' actualizado exitosamente");
            return "redirect:/clientes";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("clienteId", id);
            model.addAttribute("titulo", "Editar Cliente");
            model.addAttribute("esNuevo", false);
            return "clientes/formulario";
        }
    }
    
    /**
     * Ver detalles de un cliente con su historial de préstamos.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        ClienteResponse cliente = clienteService.buscarPorId(id);
        List<PrestamoResponse> historialPrestamos = prestamoService.obtenerHistorialCliente(id);
        List<PrestamoResponse> prestamosActivos = prestamoService.buscarActivosPorCliente(id);
        
        model.addAttribute("cliente", cliente);
        model.addAttribute("historialPrestamos", historialPrestamos);
        model.addAttribute("prestamosActivos", prestamosActivos);
        
        return "clientes/ver";
    }
    
    /**
     * Activa un cliente.
     */
    @PostMapping("/activar/{id}")
    public String activar(@PathVariable Long id,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            ClienteResponse cliente = clienteService.activar(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cliente '" + cliente.getNombreCompleto() + "' activado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/clientes";
    }
    
    /**
     * Desactiva un cliente.
     */
    @PostMapping("/desactivar/{id}")
    public String desactivar(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            ClienteResponse cliente = clienteService.desactivar(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cliente '" + cliente.getNombreCompleto() + "' desactivado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/clientes";
    }
    
    /**
     * Elimina un cliente.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            ClienteResponse cliente = clienteService.buscarPorId(id);
            clienteService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cliente '" + cliente.getNombreCompleto() + "' eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/clientes";
    }
}
