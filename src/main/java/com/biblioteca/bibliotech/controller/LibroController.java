package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.request.LibroRequest;
import com.biblioteca.bibliotech.dto.response.AutorResponse;
import com.biblioteca.bibliotech.dto.response.CategoriaResponse;
import com.biblioteca.bibliotech.dto.response.LibroResponse;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.service.*;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Controlador para gestión de libros.
 * Accesible por ADMIN y EMPLEADO.
 */
@Controller
@RequestMapping("/libros")
public class LibroController {
    
    private final LibroService libroService;
    private final AutorService autorService;
    private final CategoriaService categoriaService;
    private final PrestamoService prestamoService;
    private final AuthService authService;
    
    public LibroController(LibroService libroService,
                           AutorService autorService,
                           CategoriaService categoriaService,
                           PrestamoService prestamoService,
                           AuthService authService) {
        this.libroService = libroService;
        this.autorService = autorService;
        this.categoriaService = categoriaService;
        this.prestamoService = prestamoService;
        this.authService = authService;
    }
    
    /**
     * Lista todos los libros.
     */
    @GetMapping
    public String listar(@RequestParam(required = false) String filtro,
                         @RequestParam(required = false) String busqueda,
                         @RequestParam(required = false) Long autorId,
                         @RequestParam(required = false) Long categoriaId,
                         Model model, 
                         HttpSession session) {
        
        authService.verificarSesionActiva(session);
        
        List<LibroResponse> libros;
        
        // Aplicar filtros
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            libros = libroService.buscarPorTermino(busqueda.trim());
            model.addAttribute("busqueda", busqueda);
        } else if (autorId != null) {
            libros = libroService.buscarPorAutor(autorId);
            model.addAttribute("autorIdSeleccionado", autorId);
        } else if (categoriaId != null) {
            libros = libroService.buscarPorCategoria(categoriaId);
            model.addAttribute("categoriaIdSeleccionada", categoriaId);
        } else if ("disponibles".equals(filtro)) {
            libros = libroService.listarDisponibles();
        } else if ("sinstock".equals(filtro)) {
            libros = libroService.listarSinStock();
        } else {
            libros = libroService.listarTodos();
        }
        
        // Datos para filtros
        List<AutorResponse> autores = autorService.listarTodos();
        List<CategoriaResponse> categorias = categoriaService.listarTodas();
        
        model.addAttribute("libros", libros);
        model.addAttribute("autores", autores);
        model.addAttribute("categorias", categorias);
        model.addAttribute("filtroActual", filtro);
        model.addAttribute("totalLibros", libroService.contarTodos());
        model.addAttribute("totalEjemplares", libroService.contarTotalEjemplares());
        model.addAttribute("ejemplaresDisponibles", libroService.contarEjemplaresDisponibles());
        
        return "libros/lista";
    }
    
    /**
     * Muestra el formulario para crear un nuevo libro.
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        List<AutorResponse> autores = autorService.listarTodos();
        List<CategoriaResponse> categorias = categoriaService.listarTodas();
        
        model.addAttribute("libroRequest", new LibroRequest());
        model.addAttribute("autores", autores);
        model.addAttribute("categorias", categorias);
        model.addAttribute("titulo", "Nuevo Libro");
        model.addAttribute("esNuevo", true);
        
        return "libros/formulario";
    }
    
    /**
     * Procesa la creación de un nuevo libro.
     */
    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("libroRequest") LibroRequest request,
                        BindingResult result,
                        Model model,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("autores", autorService.listarTodos());
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("titulo", "Nuevo Libro");
            model.addAttribute("esNuevo", true);
            return "libros/formulario";
        }
        
        try {
            LibroResponse libro = libroService.crear(request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Libro '" + libro.getTitulo() + "' registrado exitosamente");
            return "redirect:/libros";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("autores", autorService.listarTodos());
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("titulo", "Nuevo Libro");
            model.addAttribute("esNuevo", true);
            return "libros/formulario";
        }
    }
    
    /**
     * Muestra el formulario para editar un libro existente.
     */
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, 
                                           Model model, 
                                           HttpSession session) {
        authService.verificarSesionActiva(session);
        
        LibroResponse libro = libroService.buscarPorId(id);
        
        // Convertir Response a Request para el formulario
        LibroRequest request = new LibroRequest();
        request.setTitulo(libro.getTitulo());
        request.setIsbn(libro.getIsbn());
        request.setAnioPublicacion(libro.getAnioPublicacion());
        request.setCantidadTotal(libro.getCantidadTotal());
        request.setIdAutor(libro.getIdAutor());
        request.setIdCategoria(libro.getIdCategoria());
        
        List<AutorResponse> autores = autorService.listarTodos();
        List<CategoriaResponse> categorias = categoriaService.listarTodas();
        
        model.addAttribute("libroRequest", request);
        model.addAttribute("libroId", id);
        model.addAttribute("autores", autores);
        model.addAttribute("categorias", categorias);
        model.addAttribute("titulo", "Editar Libro");
        model.addAttribute("esNuevo", false);
        model.addAttribute("cantidadPrestada", libro.getCantidadPrestada());
        
        return "libros/formulario";
    }
    
    /**
     * Procesa la actualización de un libro.
     */
    @PostMapping("/editar/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("libroRequest") LibroRequest request,
                             BindingResult result,
                             Model model,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        if (result.hasErrors()) {
            model.addAttribute("libroId", id);
            model.addAttribute("autores", autorService.listarTodos());
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("titulo", "Editar Libro");
            model.addAttribute("esNuevo", false);
            return "libros/formulario";
        }
        
        try {
            LibroResponse libro = libroService.actualizar(id, request);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Libro '" + libro.getTitulo() + "' actualizado exitosamente");
            return "redirect:/libros";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("libroId", id);
            model.addAttribute("autores", autorService.listarTodos());
            model.addAttribute("categorias", categoriaService.listarTodas());
            model.addAttribute("titulo", "Editar Libro");
            model.addAttribute("esNuevo", false);
            return "libros/formulario";
        }
    }
    
    /**
     * Ver detalles de un libro con su historial de préstamos.
     */
    @GetMapping("/ver/{id}")
    public String ver(@PathVariable Long id, Model model, HttpSession session) {
        authService.verificarSesionActiva(session);
        
        LibroResponse libro = libroService.buscarPorId(id);
        List<PrestamoResponse> historialPrestamos = prestamoService.buscarPorLibro(id);
        
        model.addAttribute("libro", libro);
        model.addAttribute("historialPrestamos", historialPrestamos);
        
        return "libros/ver";
    }
    
    /**
     * Elimina un libro.
     */
    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        
        authService.verificarSesionActiva(session);
        
        try {
            LibroResponse libro = libroService.buscarPorId(id);
            libroService.eliminar(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Libro '" + libro.getTitulo() + "' eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        
        return "redirect:/libros";
    }
}
