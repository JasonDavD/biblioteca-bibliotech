package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.request.AutorRequest;
import com.biblioteca.bibliotech.dto.response.AutorResponse;
import com.biblioteca.bibliotech.dto.response.LibroResponse;
import com.biblioteca.bibliotech.service.AuthService;
import com.biblioteca.bibliotech.service.AutorService;
import com.biblioteca.bibliotech.service.LibroService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para gestión de autores.
 * Accesible por ADMIN y EMPLEADO.
 */
@Controller
@RequestMapping("/autores")
public class AutorController {
    
    private final AutorService autorService;
    private final LibroService libroService;
    private final AuthService authService;
    
    public AutorController(AutorService autorService, 
                           LibroService libroService,
                           AuthService authService) {
        this.autorService = autorService;
        this.libroService = libroService;
        this.authService = authService;
    }
    
    /**
     * Lista todos los autores.
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String busqueda,
                         @RequestParam(required = false) String nacionalidad,
                         Model model, 
                         HttpSession session) {
        
        authService.verificarSesionActiva(session);
        
        List<AutorResponse> autores;
        
        // Aplicar filtros
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            autores = autorService.buscarPorNombreParcial(busqueda.trim());
            model.addAttribute("busqueda", busqueda);
        } else if (nacionalidad != null && !nacionalidad.trim().isEmpty()) {
            autores = autorService.buscarPorNacionalidad(nacionalidad);
            model.addAttribute("nacionalidadSeleccionada", nacionalidad);
        } else {
            autores = autorService.listarTodos();
        }
        
        // Datos para filtros
        List<String> nacionalidades = autorService.listarNacionalidades();
        
        model.addAttribute("autores", autores);
        model.addAttribute("nacionalidades", nacionalidades);
        model.addAttribute("totalAutores", autorService.contarTodos());
        
        return "autores/lista";
    }
    
    /**
     * Muestra el formulario para crear un nuevo autor.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        model.addAttribute("autorRequest", new AutorRequest());
        model.addAttribute("titulo", "Nuevo Autor");
        model.addAttribute("esNuevo", true);
        
        return "autores/formulario";
    }
    
    /**
     * Procesa la creación de un nuevo autor.
     */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("autorRequest") AutorRequest request,
                        BindingResult result,
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nuevo Autor");
            model.addAttribute("esNuevo", true);
            return "autores/formulario";
        }
        
        try {
            AutorResponse autor = autorService.crear(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Autor '" + autor.getNombre() + "' registrado exitosamente");
            return "redirect:/autores";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("titulo", "Nuevo Autor");
            model.addAttribute("esNuevo", true);
            return "autores/formulario";
        }
    }
    
    /**
     * Muestra el formulario para editar un autor existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, 
                                           Model model, 
                                           HttpSession session) {
        authService.verificarSesionActiva(session);
        
        AutorResponse autor = autorService.buscarPorId(id);
        
        // Convertir Response a Request para el formulario
        AutorRequest request = new AutorRequest();
        request.setNombre(autor.getNombre());
        request.setNacionalidad(autor.getNacionalidad());
        
        model.addAttribute("autorRequest", request);
        model.addAttribute("autorId", id);
        model.addAttribute("titulo", "Editar Autor");
        model.addAttribute("esNuevo", false);
        
        return "autores/formulario";
    }
    
    /**
     * Procesa la actualización de un autor.
     */
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("autorRequest") AutorRequest request,
                             BindingResult result,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("autorId", id);
            model.addAttribute("titulo", "Editar Autor");
            model.addAttribute("esNuevo", false);
            return "autores/formulario";
        }
        
        try {
            AutorResponse autor = autorService.actualizar(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Autor '" + autor.getNombre() + "' actualizado exitosamente");
            return "redirect:/autores";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("autorId", id);
            model.addAttribute("titulo", "Editar Autor");
            model.addAttribute("esNuevo", false);
            return "autores/formulario";
        }
    }
    
    /**
     * Ver detalles de un autor con sus libros.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        AutorResponse autor = autorService.buscarPorId(id);
        List<LibroResponse> libros = libroService.buscarPorAutor(id);
        
        model.addAttribute("autor", autor);
        model.addAttribute("libros", libros);
        
        return "autores/ver";
    }
    
    /**
     * Elimina un autor.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            AutorResponse autor = autorService.buscarPorId(id);
            autorService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Autor '" + autor.getNombre() + "' eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/autores";
    }
}
