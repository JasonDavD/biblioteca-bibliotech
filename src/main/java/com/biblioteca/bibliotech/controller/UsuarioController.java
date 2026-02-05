package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.request.UsuarioSistemaRequest;
import com.biblioteca.bibliotech.dto.response.UsuarioSistemaResponse;
import com.biblioteca.bibliotech.enums.Rol;
import com.biblioteca.bibliotech.service.AuthService;
import com.biblioteca.bibliotech.service.UsuarioSistemaService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para gestión de usuarios del sistema.
 * Solo accesible por usuarios con rol ADMIN.
 */
@Controller
@RequestMapping("/usuarios")
public class UsuarioController {
    
    private final UsuarioSistemaService usuarioService;
    private final AuthService authService;
    
    public UsuarioController(UsuarioSistemaService usuarioService, AuthService authService) {
        this.usuarioService = usuarioService;
        this.authService = authService;
    }
    
    /**
     * Lista todos los usuarios del sistema.
     */
    @GetMapping
    public String listar(Model model, HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        List<UsuarioSistemaResponse> usuarios = usuarioService.listarTodos();
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("totalUsuarios", usuarios.size());
        model.addAttribute("totalAdmins", usuarioService.contarPorRol(Rol.ADMIN));
        model.addAttribute("totalEmpleados", usuarioService.contarPorRol(Rol.EMPLEADO));
        
        return "usuarios/lista";
    }
    
    /**
     * Muestra el formulario para crear un nuevo usuario.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        model.addAttribute("usuarioRequest", new UsuarioSistemaRequest());
        model.addAttribute("roles", Rol.values());
        model.addAttribute("titulo", "Nuevo Usuario");
        model.addAttribute("esNuevo", true);
        
        return "usuarios/formulario";
    }
    
    /**
     * Procesa la creación de un nuevo usuario.
     */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("usuarioRequest") UsuarioSistemaRequest request,
                        BindingResult result,
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        authService.verificarAccesoAdmin(session);
        
        if (result.hasErrors()) {
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Nuevo Usuario");
            model.addAttribute("esNuevo", true);
            return "usuarios/formulario";
        }
        
        try {
            usuarioService.crear(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Usuario '" + request.getUsername() + "' creado exitosamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Nuevo Usuario");
            model.addAttribute("esNuevo", true);
            return "usuarios/formulario";
        }
    }
    
    /**
     * Muestra el formulario para editar un usuario existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, 
                                           Model model, 
                                           HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        UsuarioSistemaResponse usuario = usuarioService.buscarPorId(id);
        
        // Convertir Response a Request para el formulario
        UsuarioSistemaRequest request = new UsuarioSistemaRequest();
        request.setNombre(usuario.getNombre());
        request.setUsername(usuario.getUsername());
        request.setPassword(""); // No mostrar password actual
        request.setRol(usuario.getRol());
        
        model.addAttribute("usuarioRequest", request);
        model.addAttribute("usuarioId", id);
        model.addAttribute("roles", Rol.values());
        model.addAttribute("titulo", "Editar Usuario");
        model.addAttribute("esNuevo", false);
        
        return "usuarios/formulario";
    }
    
    /**
     * Procesa la actualización de un usuario.
     */
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("usuarioRequest") UsuarioSistemaRequest request,
                             BindingResult result,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        authService.verificarAccesoAdmin(session);
        
        if (result.hasErrors()) {
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Editar Usuario");
            model.addAttribute("esNuevo", false);
            return "usuarios/formulario";
        }
        
        try {
            usuarioService.actualizar(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Usuario actualizado exitosamente");
            return "redirect:/usuarios";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("usuarioId", id);
            model.addAttribute("roles", Rol.values());
            model.addAttribute("titulo", "Editar Usuario");
            model.addAttribute("esNuevo", false);
            return "usuarios/formulario";
        }
    }
    
    /**
     * Elimina un usuario.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        authService.verificarAccesoAdmin(session);
        
        try {
            UsuarioSistemaResponse usuario = usuarioService.buscarPorId(id);
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Usuario '" + usuario.getUsername() + "' eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/usuarios";
    }
    
    /**
     * Ver detalles de un usuario.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, HttpSession session) {
        authService.verificarAccesoAdmin(session);
        
        UsuarioSistemaResponse usuario = usuarioService.buscarPorId(id);
        model.addAttribute("usuario", usuario);
        
        return "usuarios/ver";
    }
}
