package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.request.LoginRequest;
import com.biblioteca.bibliotech.dto.response.UsuarioSistemaResponse;
import com.biblioteca.bibliotech.exception.UnauthorizedException;
import com.biblioteca.bibliotech.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador para manejo de autenticación.
 * Gestiona el login y logout de usuarios.
 */
@Controller
public class LoginController {
    
    private final AuthService authService;
    
    public LoginController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Muestra la página de login.
     */
    @GetMapping("/login")
    public String mostrarLogin(Model model, HttpSession session) {
        // Si ya está logueado, redirigir al dashboard
        if (authService.isLogueado(session)) {
            return "redirect:/dashboard";
        }
        
        model.addAttribute("loginRequest", new LoginRequest());
        return "auth/login";
    }
    
    /**
     * Procesa el formulario de login.
     */
    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
                                BindingResult result,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        // Validar errores de formulario
        if (result.hasErrors()) {
            return "auth/login";
        }
        
        try {
            // Intentar login
            UsuarioSistemaResponse usuario = authService.login(loginRequest, session);
            
            // Mensaje de bienvenida
            redirectAttributes.addFlashAttribute("successMessage", 
                    "¡Bienvenido(a), " + usuario.getNombre() + "!");
            
            return "redirect:/dashboard";
            
        } catch (UnauthorizedException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/login";
        }
    }
    
    /**
     * Cierra la sesión del usuario.
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        authService.logout(session);
        redirectAttributes.addFlashAttribute("successMessage", "Sesión cerrada correctamente");
        return "redirect:/login";
    }
}
