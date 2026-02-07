package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.request.CategoriaRequest;
import com.biblioteca.bibliotech.dto.response.CategoriaResponse;
import com.biblioteca.bibliotech.dto.response.LibroResponse;
import com.biblioteca.bibliotech.service.AuthService;
import com.biblioteca.bibliotech.service.CategoriaService;
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
 * Controlador para gestión de categorías.
 * Accesible por ADMIN y EMPLEADO.
 */
@Controller
@RequestMapping("/categorias")
public class CategoriaController {
    
    private final CategoriaService categoriaService;
    private final LibroService libroService;
    private final AuthService authService;
    
    public CategoriaController(CategoriaService categoriaService,
                               LibroService libroService,
                               AuthService authService) {
        this.categoriaService = categoriaService;
        this.libroService = libroService;
        this.authService = authService;
    }
    
    /**
     * Lista todas las categorías.
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String busqueda,
                         Model model, 
                         HttpSession session) {
        
        authService.verificarSesionActiva(session);
        
        List<CategoriaResponse> categorias;
        
        // Aplicar búsqueda
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            categorias = categoriaService.buscarPorNombreParcial(busqueda.trim());
            model.addAttribute("busqueda", busqueda);
        } else {
            categorias = categoriaService.listarTodas();
        }
        
        model.addAttribute("categorias", categorias);
        model.addAttribute("totalCategorias", categoriaService.contarTodas());
        
        return "categorias/lista";
    }
    
    /**
     * Muestra el formulario para crear una nueva categoría.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        model.addAttribute("categoriaRequest", new CategoriaRequest());
        model.addAttribute("titulo", "Nueva Categoría");
        model.addAttribute("esNuevo", true);
        
        return "categorias/formulario";
    }
    
    /**
     * Procesa la creación de una nueva categoría.
     */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("categoriaRequest") CategoriaRequest request,
                        BindingResult result,
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("titulo", "Nueva Categoría");
            model.addAttribute("esNuevo", true);
            return "categorias/formulario";
        }
        
        try {
            CategoriaResponse categoria = categoriaService.crear(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Categoría '" + categoria.getNombre() + "' registrada exitosamente");
            return "redirect:/categorias";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("titulo", "Nueva Categoría");
            model.addAttribute("esNuevo", true);
            return "categorias/formulario";
        }
    }
    
    /**
     * Muestra el formulario para editar una categoría existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, 
                                           Model model, 
                                           HttpSession session) {
        authService.verificarSesionActiva(session);
        
        CategoriaResponse categoria = categoriaService.buscarPorId(id);
        
        // Convertir Response a Request para el formulario
        CategoriaRequest request = new CategoriaRequest();
        request.setNombre(categoria.getNombre());
        request.setDescripcion(categoria.getDescripcion());
        
        model.addAttribute("categoriaRequest", request);
        model.addAttribute("categoriaId", id);
        model.addAttribute("titulo", "Editar Categoría");
        model.addAttribute("esNuevo", false);
        
        return "categorias/formulario";
    }
    
    /**
     * Procesa la actualización de una categoría.
     */
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("categoriaRequest") CategoriaRequest request,
                             BindingResult result,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("categoriaId", id);
            model.addAttribute("titulo", "Editar Categoría");
            model.addAttribute("esNuevo", false);
            return "categorias/formulario";
        }
        
        try {
            CategoriaResponse categoria = categoriaService.actualizar(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Categoría '" + categoria.getNombre() + "' actualizada exitosamente");
            return "redirect:/categorias";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categoriaId", id);
            model.addAttribute("titulo", "Editar Categoría");
            model.addAttribute("esNuevo", false);
            return "categorias/formulario";
        }
    }
    
    /**
     * Ver detalles de una categoría con sus libros.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        CategoriaResponse categoria = categoriaService.buscarPorId(id);
        List<LibroResponse> libros = libroService.buscarPorCategoria(id);
        
        model.addAttribute("categoria", categoria);
        model.addAttribute("libros", libros);
        
        return "categorias/ver";
    }
    
    /**
     * Elimina una categoría.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            CategoriaResponse categoria = categoriaService.buscarPorId(id);
            categoriaService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Categoría '" + categoria.getNombre() + "' eliminada exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/categorias";
    }
}
