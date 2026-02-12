package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar reportes en formato PDF.
 */
@Service
public class PdfExportService {

    private static final BaseColor COLOR_PRIMARIO = new BaseColor(44, 62, 80);
    private static final BaseColor COLOR_ENCABEZADO_TABLA = new BaseColor(52, 73, 94);
    private static final BaseColor COLOR_FILA_ALTERNA = new BaseColor(241, 245, 249);
    private static final BaseColor COLOR_BLANCO = BaseColor.WHITE;
    private static final BaseColor COLOR_TEXTO = new BaseColor(33, 37, 41);
    private static final BaseColor COLOR_GRIS = new BaseColor(108, 117, 125);
    private static final BaseColor COLOR_DANGER = new BaseColor(220, 53, 69);
    private static final BaseColor COLOR_SECCION = new BaseColor(23, 162, 184);

    private static final Font FUENTE_TITULO_SISTEMA = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, COLOR_PRIMARIO);
    private static final Font FUENTE_TITULO_REPORTE = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, COLOR_TEXTO);
    private static final Font FUENTE_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, COLOR_GRIS);
    private static final Font FUENTE_ENCABEZADO_TABLA = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_BLANCO);
    private static final Font FUENTE_CELDA = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, COLOR_TEXTO);
    private static final Font FUENTE_CELDA_NEGRITA = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_TEXTO);
    private static final Font FUENTE_RESUMEN = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_PRIMARIO);
    private static final Font FUENTE_PIE = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, COLOR_GRIS);
    private static final Font FUENTE_SECCION = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD, COLOR_SECCION);
    private static final Font FUENTE_ETIQUETA = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, COLOR_GRIS);
    private static final Font FUENTE_VALOR = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, COLOR_TEXTO);

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera PDF del reporte de prestamos vencidos.
     */
    public byte[] generarPdfPrestamosVencidos(List<PrestamoResponse> prestamos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(documento, baos);
        documento.open();

        agregarEncabezado(documento, "Reporte de Prestamos Vencidos");

        // Tabla
        PdfPTable tabla = new PdfPTable(7);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(15f);
        tabla.setWidths(new float[]{1f, 3f, 2.5f, 1.5f, 1.5f, 1.5f, 1.5f});

        // Encabezados
        String[] encabezados = {"ID", "Libro", "Cliente", "DNI", "Fecha Prestamo", "Fecha Esperada", "Dias Retraso"};
        for (String enc : encabezados) {
            tabla.addCell(crearCeldaEncabezado(enc));
        }

        // Datos
        int fila = 0;
        for (PrestamoResponse p : prestamos) {
            boolean filaAlterna = fila % 2 != 0;

            tabla.addCell(crearCelda(String.valueOf(p.getId()), Element.ALIGN_CENTER, filaAlterna));
            tabla.addCell(crearCelda(p.getTituloLibro() != null ? p.getTituloLibro() : "-", Element.ALIGN_LEFT, filaAlterna));
            tabla.addCell(crearCelda(p.getNombreCompletoCliente() != null ? p.getNombreCompletoCliente() : "-", Element.ALIGN_LEFT, filaAlterna));
            tabla.addCell(crearCelda(p.getDniCliente() != null ? p.getDniCliente() : "-", Element.ALIGN_CENTER, filaAlterna));
            tabla.addCell(crearCelda(p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA) : "-", Element.ALIGN_CENTER, filaAlterna));
            tabla.addCell(crearCelda(p.getFechaDevolucionEsperada() != null ? p.getFechaDevolucionEsperada().format(FORMATO_FECHA) : "-", Element.ALIGN_CENTER, filaAlterna));

            PdfPCell celdaDias = crearCelda(p.getDiasRetraso() != null ? p.getDiasRetraso() + " dias" : "0 dias", Element.ALIGN_CENTER, filaAlterna);
            celdaDias.setPhrase(new Phrase(p.getDiasRetraso() != null ? p.getDiasRetraso() + " dias" : "0 dias",
                    new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_DANGER)));
            tabla.addCell(celdaDias);

            fila++;
        }

        documento.add(tabla);

        // Resumen
        Paragraph resumen = new Paragraph();
        resumen.setSpacingBefore(15f);
        resumen.add(new Chunk("Total de prestamos vencidos: " + prestamos.size(), FUENTE_RESUMEN));
        documento.add(resumen);

        agregarPiePagina(documento);
        documento.close();

        return baos.toByteArray();
    }

    /**
     * Genera PDF del reporte de clientes morosos.
     */
    public byte[] generarPdfClientesMorosos(List<ClienteResponse> clientes) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4.rotate(), 36, 36, 36, 36);
        PdfWriter.getInstance(documento, baos);
        documento.open();

        agregarEncabezado(documento, "Reporte de Clientes Morosos");

        // Tabla
        PdfPTable tabla = new PdfPTable(6);
        tabla.setWidthPercentage(100);
        tabla.setSpacingBefore(15f);
        tabla.setWidths(new float[]{2.5f, 1.5f, 2.5f, 1.5f, 1.5f, 1.5f});

        // Encabezados
        String[] encabezados = {"Cliente", "DNI", "Email", "Telefono", "Prestamos Activos", "Prestamos Vencidos"};
        for (String enc : encabezados) {
            tabla.addCell(crearCeldaEncabezado(enc));
        }

        // Datos
        int fila = 0;
        for (ClienteResponse c : clientes) {
            boolean filaAlterna = fila % 2 != 0;

            tabla.addCell(crearCelda(c.getNombreCompleto() != null ? c.getNombreCompleto() : "-", Element.ALIGN_LEFT, filaAlterna));
            tabla.addCell(crearCelda(c.getDni() != null ? c.getDni() : "-", Element.ALIGN_CENTER, filaAlterna));
            tabla.addCell(crearCelda(c.getEmail() != null ? c.getEmail() : "-", Element.ALIGN_LEFT, filaAlterna));
            tabla.addCell(crearCelda(c.getTelefono() != null ? c.getTelefono() : "-", Element.ALIGN_CENTER, filaAlterna));
            tabla.addCell(crearCelda(c.getPrestamosActivos() != null ? String.valueOf(c.getPrestamosActivos()) : "0", Element.ALIGN_CENTER, filaAlterna));

            PdfPCell celdaVencidos = crearCelda("", Element.ALIGN_CENTER, filaAlterna);
            String valorVencidos = c.getPrestamosVencidos() != null ? String.valueOf(c.getPrestamosVencidos()) : "0";
            celdaVencidos.setPhrase(new Phrase(valorVencidos, new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, COLOR_DANGER)));
            tabla.addCell(celdaVencidos);

            fila++;
        }

        documento.add(tabla);

        // Resumen
        Paragraph resumen = new Paragraph();
        resumen.setSpacingBefore(15f);
        resumen.add(new Chunk("Total de clientes morosos: " + clientes.size(), FUENTE_RESUMEN));
        documento.add(resumen);

        agregarPiePagina(documento);
        documento.close();

        return baos.toByteArray();
    }

    /**
     * Genera PDF del reporte de estadisticas generales.
     */
    public byte[] generarPdfEstadisticas(Map<String, Object> datos) throws DocumentException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document documento = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(documento, baos);
        documento.open();

        agregarEncabezado(documento, "Estadisticas del Sistema");

        // Seccion: Biblioteca
        agregarSeccionEstadisticas(documento, "Biblioteca", new String[][]{
                {"Titulos Registrados", String.valueOf(datos.getOrDefault("totalLibros", 0))},
                {"Total Ejemplares", String.valueOf(datos.getOrDefault("totalEjemplares", 0))},
                {"Ejemplares Disponibles", String.valueOf(datos.getOrDefault("ejemplaresDisponibles", 0))},
                {"Ejemplares Prestados", String.valueOf(datos.getOrDefault("ejemplaresPrestados", 0))}
        });

        // Seccion: Clientes
        agregarSeccionEstadisticas(documento, "Clientes", new String[][]{
                {"Total Clientes", String.valueOf(datos.getOrDefault("totalClientes", 0))},
                {"Clientes Activos", String.valueOf(datos.getOrDefault("clientesActivos", 0))},
                {"Clientes Inactivos", String.valueOf(datos.getOrDefault("clientesInactivos", 0))}
        });

        // Seccion: Prestamos
        agregarSeccionEstadisticas(documento, "Prestamos", new String[][]{
                {"Prestamos Activos", String.valueOf(datos.getOrDefault("prestamosActivos", 0))},
                {"Prestamos Vencidos", String.valueOf(datos.getOrDefault("prestamosVencidos", 0))},
                {"Prestamos Hoy", String.valueOf(datos.getOrDefault("prestamosHoy", 0))},
                {"Devoluciones Hoy", String.valueOf(datos.getOrDefault("devolucionesHoy", 0))}
        });

        // Seccion: Catalogo
        agregarSeccionEstadisticas(documento, "Catalogo", new String[][]{
                {"Total Autores", String.valueOf(datos.getOrDefault("totalAutores", 0))},
                {"Total Categorias", String.valueOf(datos.getOrDefault("totalCategorias", 0))}
        });

        agregarPiePagina(documento);
        documento.close();

        return baos.toByteArray();
    }

    // --- Helpers privados ---

    private void agregarEncabezado(Document documento, String tituloReporte) throws DocumentException {
        Paragraph branding = new Paragraph("BiblioTech", FUENTE_TITULO_SISTEMA);
        branding.setAlignment(Element.ALIGN_LEFT);
        documento.add(branding);

        Paragraph titulo = new Paragraph(tituloReporte, FUENTE_TITULO_REPORTE);
        titulo.setSpacingBefore(5f);
        documento.add(titulo);

        Paragraph fecha = new Paragraph("Fecha de generacion: " + LocalDate.now().format(FORMATO_FECHA), FUENTE_SUBTITULO);
        fecha.setSpacingBefore(3f);
        documento.add(fecha);

        LineSeparator separador = new LineSeparator();
        separador.setLineColor(COLOR_PRIMARIO);
        separador.setLineWidth(1f);
        documento.add(new Chunk(separador));
    }

    private PdfPCell crearCeldaEncabezado(String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_ENCABEZADO_TABLA));
        celda.setBackgroundColor(COLOR_ENCABEZADO_TABLA);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(8f);
        celda.setBorderColor(COLOR_ENCABEZADO_TABLA);
        return celda;
    }

    private PdfPCell crearCelda(String texto, int alineacion, boolean filaAlterna) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, FUENTE_CELDA));
        celda.setHorizontalAlignment(alineacion);
        celda.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celda.setPadding(6f);
        celda.setBorderColor(new BaseColor(222, 226, 230));
        if (filaAlterna) {
            celda.setBackgroundColor(COLOR_FILA_ALTERNA);
        }
        return celda;
    }

    private void agregarPiePagina(Document documento) throws DocumentException {
        documento.add(Chunk.NEWLINE);
        LineSeparator separador = new LineSeparator();
        separador.setLineColor(COLOR_GRIS);
        separador.setLineWidth(0.5f);
        documento.add(new Chunk(separador));

        Paragraph pie = new Paragraph("Reporte generado automaticamente por BiblioTech - " + LocalDate.now().format(FORMATO_FECHA), FUENTE_PIE);
        pie.setSpacingBefore(5f);
        pie.setAlignment(Element.ALIGN_CENTER);
        documento.add(pie);
    }

    private void agregarSeccionEstadisticas(Document documento, String tituloSeccion, String[][] datos) throws DocumentException {
        Paragraph titulo = new Paragraph(tituloSeccion, FUENTE_SECCION);
        titulo.setSpacingBefore(20f);
        titulo.setSpacingAfter(8f);
        documento.add(titulo);

        PdfPTable tabla = new PdfPTable(2);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3f, 2f});

        for (int i = 0; i < datos.length; i++) {
            boolean filaAlterna = i % 2 != 0;

            PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(datos[i][0], FUENTE_ETIQUETA));
            celdaEtiqueta.setHorizontalAlignment(Element.ALIGN_LEFT);
            celdaEtiqueta.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaEtiqueta.setPadding(8f);
            celdaEtiqueta.setBorderColor(new BaseColor(222, 226, 230));
            if (filaAlterna) {
                celdaEtiqueta.setBackgroundColor(COLOR_FILA_ALTERNA);
            }
            tabla.addCell(celdaEtiqueta);

            PdfPCell celdaValor = new PdfPCell(new Phrase(datos[i][1], FUENTE_VALOR));
            celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
            celdaValor.setVerticalAlignment(Element.ALIGN_MIDDLE);
            celdaValor.setPadding(8f);
            celdaValor.setBorderColor(new BaseColor(222, 226, 230));
            if (filaAlterna) {
                celdaValor.setBackgroundColor(COLOR_FILA_ALTERNA);
            }
            tabla.addCell(celdaValor);
        }

        documento.add(tabla);
    }
}
