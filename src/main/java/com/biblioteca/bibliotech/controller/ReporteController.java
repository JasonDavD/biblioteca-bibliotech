package com.biblioteca.bibliotech.controller;

import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.service.*;
import com.itextpdf.text.DocumentException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final PdfExportService pdfExportService;
    private final ExcelExportService excelExportService;

    public ReporteController(AuthService authService,
                             PrestamoService prestamoService,
                             ClienteService clienteService,
                             LibroService libroService,
                             AutorService autorService,
                             CategoriaService categoriaService,
                             PdfExportService pdfExportService,
                             ExcelExportService excelExportService) {
        this.authService = authService;
        this.prestamoService = prestamoService;
        this.clienteService = clienteService;
        this.libroService = libroService;
        this.autorService = autorService;
        this.categoriaService = categoriaService;
        this.pdfExportService = pdfExportService;
        this.excelExportService = excelExportService;
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

    /**
     * Exportar reporte de prestamos vencidos en PDF.
     */
    @GetMapping("/prestamos-vencidos/pdf")
    public ResponseEntity<byte[]> exportarPrestamosVencidosPdf(HttpSession session) throws DocumentException {
        authService.verificarAccesoAdmin(session);

        prestamoService.actualizarPrestamosVencidos();
        List<PrestamoResponse> prestamosVencidos = prestamoService.listarVencidosConDetalles();

        byte[] pdfBytes = pdfExportService.generarPdfPrestamosVencidos(prestamosVencidos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "prestamos_vencidos.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Exportar reporte de clientes morosos en PDF.
     */
    @GetMapping("/clientes-morosos/pdf")
    public ResponseEntity<byte[]> exportarClientesMorososPdf(HttpSession session) throws DocumentException {
        authService.verificarAccesoAdmin(session);

        List<ClienteResponse> clientesMorosos = clienteService.listarConPrestamosVencidos();

        byte[] pdfBytes = pdfExportService.generarPdfClientesMorosos(clientesMorosos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "clientes_morosos.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Exportar reporte de estadisticas en PDF.
     */
    @GetMapping("/estadisticas/pdf")
    public ResponseEntity<byte[]> exportarEstadisticasPdf(HttpSession session) throws DocumentException {
        authService.verificarAccesoAdmin(session);

        Map<String, Object> datos = new HashMap<>();
        datos.put("totalLibros", libroService.contarTodos());
        datos.put("totalEjemplares", libroService.contarTotalEjemplares());
        datos.put("ejemplaresDisponibles", libroService.contarEjemplaresDisponibles());
        datos.put("ejemplaresPrestados", libroService.contarTotalEjemplares() - libroService.contarEjemplaresDisponibles());
        datos.put("totalClientes", clienteService.contarTodos());
        datos.put("clientesActivos", clienteService.contarActivos());
        datos.put("clientesInactivos", clienteService.contarInactivos());
        datos.put("prestamosActivos", prestamoService.contarActivos());
        datos.put("prestamosVencidos", prestamoService.contarVencidos());
        datos.put("prestamosHoy", prestamoService.contarPrestamosHoy());
        datos.put("devolucionesHoy", prestamoService.contarDevolucionesHoy());
        datos.put("totalAutores", autorService.contarTodos());
        datos.put("totalCategorias", categoriaService.contarTodas());

        byte[] pdfBytes = pdfExportService.generarPdfEstadisticas(datos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "estadisticas.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    /**
     * Exportar reporte de prestamos vencidos en Excel.
     */
    @GetMapping("/prestamos-vencidos/excel")
    public ResponseEntity<byte[]> exportarPrestamosVencidosExcel(HttpSession session) throws IOException {
        authService.verificarAccesoAdmin(session);

        prestamoService.actualizarPrestamosVencidos();
        List<PrestamoResponse> prestamosVencidos = prestamoService.listarVencidosConDetalles();

        byte[] excelBytes = excelExportService.generarExcelPrestamosVencidos(prestamosVencidos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "prestamos_vencidos.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /**
     * Exportar reporte de clientes morosos en Excel.
     */
    @GetMapping("/clientes-morosos/excel")
    public ResponseEntity<byte[]> exportarClientesMorososExcel(HttpSession session) throws IOException {
        authService.verificarAccesoAdmin(session);

        List<ClienteResponse> clientesMorosos = clienteService.listarConPrestamosVencidos();

        byte[] excelBytes = excelExportService.generarExcelClientesMorosos(clientesMorosos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "clientes_morosos.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }

    /**
     * Exportar reporte de estadisticas en Excel.
     */
    @GetMapping("/estadisticas/excel")
    public ResponseEntity<byte[]> exportarEstadisticasExcel(HttpSession session) throws IOException {
        authService.verificarAccesoAdmin(session);

        Map<String, Object> datos = new HashMap<>();
        datos.put("totalLibros", libroService.contarTodos());
        datos.put("totalEjemplares", libroService.contarTotalEjemplares());
        datos.put("ejemplaresDisponibles", libroService.contarEjemplaresDisponibles());
        datos.put("ejemplaresPrestados", libroService.contarTotalEjemplares() - libroService.contarEjemplaresDisponibles());
        datos.put("totalClientes", clienteService.contarTodos());
        datos.put("clientesActivos", clienteService.contarActivos());
        datos.put("clientesInactivos", clienteService.contarInactivos());
        datos.put("prestamosActivos", prestamoService.contarActivos());
        datos.put("prestamosVencidos", prestamoService.contarVencidos());
        datos.put("prestamosHoy", prestamoService.contarPrestamosHoy());
        datos.put("devolucionesHoy", prestamoService.contarDevolucionesHoy());
        datos.put("totalAutores", autorService.contarTodos());
        datos.put("totalCategorias", categoriaService.contarTodas());

        byte[] excelBytes = excelExportService.generarExcelEstadisticas(datos);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "estadisticas.xlsx");

        return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);
    }
}
