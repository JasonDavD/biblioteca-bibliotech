package com.biblioteca.bibliotech.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Manejador global de excepciones para la aplicación.
 * Captura las excepciones y redirige a las vistas apropiadas con mensajes de error.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Maneja excepciones de recurso no encontrado.
     * Redirige a una página de error 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("titulo", "Recurso no encontrado");
        return "error/404";
    }
    
    /**
     * Maneja excepciones de reglas de negocio.
     * Muestra el mensaje de error y permite continuar la operación.
     */
    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorCodigo", ex.getCodigo());
        // Redirige a la página anterior (se manejará en cada controlador específico)
        return "redirect:/dashboard";
    }
    
    /**
     * Maneja excepciones de autenticación/autorización.
     * Redirige al login o muestra página de acceso denegado.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException ex, Model model, 
                                               RedirectAttributes redirectAttributes) {
        if (UnauthorizedException.ACCESO_DENEGADO.equals(ex.getTipo())) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("titulo", "Acceso Denegado");
            return "error/403";
        }
        
        // Para sesión expirada o sin sesión, redirigir al login
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/login";
    }
    
    /**
     * Maneja excepciones generales no controladas.
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, Model model) {
        model.addAttribute("error", "Ha ocurrido un error inesperado. Por favor, intente nuevamente.");
        model.addAttribute("detalle", ex.getMessage());
        model.addAttribute("titulo", "Error del Sistema");
        return "error/500";
    }
}
