package com.biblioteca.bibliotech.config;

import com.biblioteca.bibliotech.dto.response.UsuarioSistemaResponse;
import com.biblioteca.bibliotech.enums.Rol;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * Interceptor para control de acceso basado en sesión y roles.
 * Verifica que el usuario esté logueado y tenga los permisos necesarios.
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    
    private static final String USUARIO_SESSION_KEY = "usuarioLogueado";
    
    // Rutas que requieren rol ADMIN
    private static final String[] RUTAS_ADMIN = {
            "/usuarios",
            "/reportes"
    };
    
    // Rutas públicas (no requieren autenticación)
    private static final String[] RUTAS_PUBLICAS = {
            "/login",
            "/css",
            "/js",
            "/images",
            "/error"
    };
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                             Object handler) throws Exception {
        
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = uri.substring(contextPath.length());
        
        // Permitir rutas públicas
        if (esRutaPublica(path)) {
            return true;
        }
        
        // Verificar sesión
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(USUARIO_SESSION_KEY) == null) {
            // Redirigir al login
            response.sendRedirect(contextPath + "/login");
            return false;
        }
        
        // Obtener usuario de la sesión
        UsuarioSistemaResponse usuario = (UsuarioSistemaResponse) session.getAttribute(USUARIO_SESSION_KEY);
        
        // Verificar permisos para rutas de ADMIN
        if (requiereAdmin(path) && !Rol.ADMIN.equals(usuario.getRol())) {
            // Redirigir a página de acceso denegado
            response.sendRedirect(contextPath + "/error/403");
            return false;
        }
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler, ModelAndView modelAndView) throws Exception {
        
        // Agregar usuario al modelo para todas las vistas (si está logueado)
        if (modelAndView != null) {
            HttpSession session = request.getSession(false);
            if (session != null) {
                UsuarioSistemaResponse usuario = (UsuarioSistemaResponse) session.getAttribute(USUARIO_SESSION_KEY);
                if (usuario != null) {
                    modelAndView.addObject("usuarioLogueado", usuario);
                    modelAndView.addObject("esAdmin", Rol.ADMIN.equals(usuario.getRol()));
                }
            }
        }
    }
    
    /**
     * Verifica si la ruta es pública.
     */
    private boolean esRutaPublica(String path) {
        for (String rutaPublica : RUTAS_PUBLICAS) {
            if (path.startsWith(rutaPublica) || path.equals("/")) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Verifica si la ruta requiere rol ADMIN.
     */
    private boolean requiereAdmin(String path) {
        for (String rutaAdmin : RUTAS_ADMIN) {
            if (path.startsWith(rutaAdmin)) {
                return true;
            }
        }
        return false;
    }
}
