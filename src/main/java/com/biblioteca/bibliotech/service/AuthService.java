package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.request.LoginRequest;
import com.biblioteca.bibliotech.dto.response.UsuarioSistemaResponse;
import com.biblioteca.bibliotech.entity.UsuarioSistema;
import com.biblioteca.bibliotech.enums.Rol;
import com.biblioteca.bibliotech.exception.UnauthorizedException;
import com.biblioteca.bibliotech.mapper.UsuarioSistemaMapper;
import com.biblioteca.bibliotech.repository.UsuarioSistemaRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Servicio para autenticación y manejo de sesiones.
 * Gestiona el login, logout y verificación de permisos.
 */
@Service
public class AuthService {
    
    private static final String USUARIO_SESSION_KEY = "usuarioLogueado";
    
    private final UsuarioSistemaRepository usuarioRepository;
    private final UsuarioSistemaMapper usuarioMapper;
    
    public AuthService(UsuarioSistemaRepository usuarioRepository, 
                       UsuarioSistemaMapper usuarioMapper) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioMapper = usuarioMapper;
    }
    
    /**
     * Realiza el login del usuario.
     * Valida credenciales y guarda el usuario en sesión.
     * 
     * @param loginRequest Credenciales del usuario
     * @param session Sesión HTTP
     * @return UsuarioSistemaResponse con datos del usuario logueado
     * @throws UnauthorizedException si las credenciales son inválidas
     */
    public UsuarioSistemaResponse login(LoginRequest loginRequest, HttpSession session) {
        UsuarioSistema usuario = usuarioRepository
                .findByUsernameAndPassword(loginRequest.getUsername(), loginRequest.getPassword())
                .orElseThrow(() -> new UnauthorizedException(
                        UnauthorizedException.CREDENCIALES_INVALIDAS,
                        "Usuario o contraseña incorrectos"
                ));
        
        // Guardar usuario en sesión
        UsuarioSistemaResponse usuarioResponse = usuarioMapper.toResponse(usuario);
        session.setAttribute(USUARIO_SESSION_KEY, usuarioResponse);
        
        return usuarioResponse;
    }
    
    /**
     * Cierra la sesión del usuario.
     * 
     * @param session Sesión HTTP a invalidar
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }
    
    /**
     * Obtiene el usuario actualmente logueado.
     * 
     * @param session Sesión HTTP
     * @return UsuarioSistemaResponse del usuario logueado
     * @throws UnauthorizedException si no hay sesión activa
     */
    public UsuarioSistemaResponse getUsuarioLogueado(HttpSession session) {
        UsuarioSistemaResponse usuario = (UsuarioSistemaResponse) session.getAttribute(USUARIO_SESSION_KEY);
        
        if (usuario == null) {
            throw new UnauthorizedException(
                    UnauthorizedException.SIN_SESION,
                    "Debe iniciar sesión para acceder"
            );
        }
        
        return usuario;
    }
    
    /**
     * Verifica si hay un usuario logueado.
     * 
     * @param session Sesión HTTP
     * @return true si hay usuario logueado
     */
    public boolean isLogueado(HttpSession session) {
        return session.getAttribute(USUARIO_SESSION_KEY) != null;
    }
    
    /**
     * Verifica si el usuario logueado es ADMIN.
     * 
     * @param session Sesión HTTP
     * @return true si el usuario es ADMIN
     */
    public boolean isAdmin(HttpSession session) {
        if (!isLogueado(session)) {
            return false;
        }
        UsuarioSistemaResponse usuario = getUsuarioLogueado(session);
        return Rol.ADMIN.equals(usuario.getRol());
    }
    
    /**
     * Verifica si el usuario logueado es EMPLEADO.
     * 
     * @param session Sesión HTTP
     * @return true si el usuario es EMPLEADO
     */
    public boolean isEmpleado(HttpSession session) {
        if (!isLogueado(session)) {
            return false;
        }
        UsuarioSistemaResponse usuario = getUsuarioLogueado(session);
        return Rol.EMPLEADO.equals(usuario.getRol());
    }
    
    /**
     * Verifica que el usuario tenga rol de ADMIN.
     * Lanza excepción si no tiene permiso.
     * 
     * @param session Sesión HTTP
     * @throws UnauthorizedException si no es ADMIN
     */
    public void verificarAccesoAdmin(HttpSession session) {
        if (!isLogueado(session)) {
            throw new UnauthorizedException(
                    UnauthorizedException.SIN_SESION,
                    "Debe iniciar sesión para acceder"
            );
        }
        
        if (!isAdmin(session)) {
            throw new UnauthorizedException(
                    UnauthorizedException.ACCESO_DENEGADO,
                    "No tiene permisos para acceder a esta sección. Se requiere rol de Administrador."
            );
        }
    }
    
    /**
     * Verifica que el usuario esté logueado (cualquier rol).
     * 
     * @param session Sesión HTTP
     * @throws UnauthorizedException si no está logueado
     */
    public void verificarSesionActiva(HttpSession session) {
        if (!isLogueado(session)) {
            throw new UnauthorizedException(
                    UnauthorizedException.SIN_SESION,
                    "Debe iniciar sesión para acceder"
            );
        }
    }
}
