package com.biblioteca.bibliotech.service;

import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar reportes en formato Excel (.xlsx).
 */
@Service
public class ExcelExportService {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Genera Excel del reporte de prestamos vencidos.
     */
    public byte[] generarExcelPrestamosVencidos(List<PrestamoResponse> prestamos) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Prestamos Vencidos");

            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloCelda = crearEstiloCelda(workbook);
            CellStyle estiloCeldaCentrada = crearEstiloCeldaCentrada(workbook);
            CellStyle estiloDanger = crearEstiloDanger(workbook);

            int filaActual = 0;

            // Titulo
            Row filaTitulo = hoja.createRow(filaActual++);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("BiblioTech - Reporte de Prestamos Vencidos");
            celdaTitulo.setCellStyle(estiloTitulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 6));

            // Fecha
            Row filaFecha = hoja.createRow(filaActual++);
            Cell celdaFecha = filaFecha.createCell(0);
            celdaFecha.setCellValue("Fecha de generacion: " + LocalDate.now().format(FORMATO_FECHA));
            celdaFecha.setCellStyle(estiloSubtitulo);
            hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 6));

            // Fila vacia
            filaActual++;

            // Encabezados
            String[] encabezados = {"ID", "Libro", "Cliente", "DNI", "Fecha Prestamo", "Fecha Esperada", "Dias Retraso"};
            Row filaEncabezados = hoja.createRow(filaActual++);
            for (int i = 0; i < encabezados.length; i++) {
                Cell celda = filaEncabezados.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            // Datos
            for (PrestamoResponse p : prestamos) {
                Row fila = hoja.createRow(filaActual++);

                Cell c0 = fila.createCell(0);
                c0.setCellValue(p.getId() != null ? p.getId() : 0);
                c0.setCellStyle(estiloCeldaCentrada);

                Cell c1 = fila.createCell(1);
                c1.setCellValue(p.getTituloLibro() != null ? p.getTituloLibro() : "-");
                c1.setCellStyle(estiloCelda);

                Cell c2 = fila.createCell(2);
                c2.setCellValue(p.getNombreCompletoCliente() != null ? p.getNombreCompletoCliente() : "-");
                c2.setCellStyle(estiloCelda);

                Cell c3 = fila.createCell(3);
                c3.setCellValue(p.getDniCliente() != null ? p.getDniCliente() : "-");
                c3.setCellStyle(estiloCeldaCentrada);

                Cell c4 = fila.createCell(4);
                c4.setCellValue(p.getFechaPrestamo() != null ? p.getFechaPrestamo().format(FORMATO_FECHA) : "-");
                c4.setCellStyle(estiloCeldaCentrada);

                Cell c5 = fila.createCell(5);
                c5.setCellValue(p.getFechaDevolucionEsperada() != null ? p.getFechaDevolucionEsperada().format(FORMATO_FECHA) : "-");
                c5.setCellStyle(estiloCeldaCentrada);

                Cell c6 = fila.createCell(6);
                c6.setCellValue(p.getDiasRetraso() != null ? p.getDiasRetraso() + " dias" : "0 dias");
                c6.setCellStyle(estiloDanger);
            }

            // Fila de resumen
            filaActual++;
            Row filaResumen = hoja.createRow(filaActual);
            Cell celdaResumen = filaResumen.createCell(0);
            celdaResumen.setCellValue("Total de prestamos vencidos: " + prestamos.size());
            celdaResumen.setCellStyle(crearEstiloResumen(workbook));
            hoja.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 6));

            // Auto-ajustar columnas
            for (int i = 0; i < encabezados.length; i++) {
                hoja.autoSizeColumn(i);
            }

            return escribirBytes(workbook);
        }
    }

    /**
     * Genera Excel del reporte de clientes morosos.
     */
    public byte[] generarExcelClientesMorosos(List<ClienteResponse> clientes) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Clientes Morosos");

            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(workbook);
            CellStyle estiloEncabezado = crearEstiloEncabezado(workbook);
            CellStyle estiloCelda = crearEstiloCelda(workbook);
            CellStyle estiloCeldaCentrada = crearEstiloCeldaCentrada(workbook);
            CellStyle estiloDanger = crearEstiloDanger(workbook);

            int filaActual = 0;

            // Titulo
            Row filaTitulo = hoja.createRow(filaActual++);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("BiblioTech - Reporte de Clientes Morosos");
            celdaTitulo.setCellStyle(estiloTitulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            // Fecha
            Row filaFecha = hoja.createRow(filaActual++);
            Cell celdaFecha = filaFecha.createCell(0);
            celdaFecha.setCellValue("Fecha de generacion: " + LocalDate.now().format(FORMATO_FECHA));
            celdaFecha.setCellStyle(estiloSubtitulo);
            hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

            // Fila vacia
            filaActual++;

            // Encabezados
            String[] encabezados = {"Cliente", "DNI", "Email", "Telefono", "Prestamos Activos", "Prestamos Vencidos"};
            Row filaEncabezados = hoja.createRow(filaActual++);
            for (int i = 0; i < encabezados.length; i++) {
                Cell celda = filaEncabezados.createCell(i);
                celda.setCellValue(encabezados[i]);
                celda.setCellStyle(estiloEncabezado);
            }

            // Datos
            for (ClienteResponse c : clientes) {
                Row fila = hoja.createRow(filaActual++);

                Cell c0 = fila.createCell(0);
                c0.setCellValue(c.getNombreCompleto() != null ? c.getNombreCompleto() : "-");
                c0.setCellStyle(estiloCelda);

                Cell c1 = fila.createCell(1);
                c1.setCellValue(c.getDni() != null ? c.getDni() : "-");
                c1.setCellStyle(estiloCeldaCentrada);

                Cell c2 = fila.createCell(2);
                c2.setCellValue(c.getEmail() != null ? c.getEmail() : "-");
                c2.setCellStyle(estiloCelda);

                Cell c3 = fila.createCell(3);
                c3.setCellValue(c.getTelefono() != null ? c.getTelefono() : "-");
                c3.setCellStyle(estiloCeldaCentrada);

                Cell c4 = fila.createCell(4);
                c4.setCellValue(c.getPrestamosActivos() != null ? c.getPrestamosActivos() : 0);
                c4.setCellStyle(estiloCeldaCentrada);

                Cell c5 = fila.createCell(5);
                c5.setCellValue(c.getPrestamosVencidos() != null ? c.getPrestamosVencidos() : 0);
                c5.setCellStyle(estiloDanger);
            }

            // Fila de resumen
            filaActual++;
            Row filaResumen = hoja.createRow(filaActual);
            Cell celdaResumen = filaResumen.createCell(0);
            celdaResumen.setCellValue("Total de clientes morosos: " + clientes.size());
            celdaResumen.setCellStyle(crearEstiloResumen(workbook));
            hoja.addMergedRegion(new CellRangeAddress(filaActual, filaActual, 0, 5));

            // Auto-ajustar columnas
            for (int i = 0; i < encabezados.length; i++) {
                hoja.autoSizeColumn(i);
            }

            return escribirBytes(workbook);
        }
    }

    /**
     * Genera Excel del reporte de estadisticas generales.
     */
    public byte[] generarExcelEstadisticas(Map<String, Object> datos) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet hoja = workbook.createSheet("Estadisticas");

            CellStyle estiloTitulo = crearEstiloTitulo(workbook);
            CellStyle estiloSubtitulo = crearEstiloSubtitulo(workbook);
            CellStyle estiloSeccion = crearEstiloSeccion(workbook);
            CellStyle estiloEtiqueta = crearEstiloCelda(workbook);
            CellStyle estiloValor = crearEstiloValor(workbook);

            int filaActual = 0;

            // Titulo
            Row filaTitulo = hoja.createRow(filaActual++);
            Cell celdaTitulo = filaTitulo.createCell(0);
            celdaTitulo.setCellValue("BiblioTech - Estadisticas del Sistema");
            celdaTitulo.setCellStyle(estiloTitulo);
            hoja.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

            // Fecha
            Row filaFecha = hoja.createRow(filaActual++);
            Cell celdaFecha = filaFecha.createCell(0);
            celdaFecha.setCellValue("Fecha de generacion: " + LocalDate.now().format(FORMATO_FECHA));
            celdaFecha.setCellStyle(estiloSubtitulo);
            hoja.addMergedRegion(new CellRangeAddress(1, 1, 0, 1));

            // Seccion: Biblioteca
            filaActual++;
            filaActual = agregarSeccion(hoja, filaActual, "Biblioteca", estiloSeccion, estiloEtiqueta, estiloValor, new String[][]{
                    {"Titulos Registrados", String.valueOf(datos.getOrDefault("totalLibros", 0))},
                    {"Total Ejemplares", String.valueOf(datos.getOrDefault("totalEjemplares", 0))},
                    {"Ejemplares Disponibles", String.valueOf(datos.getOrDefault("ejemplaresDisponibles", 0))},
                    {"Ejemplares Prestados", String.valueOf(datos.getOrDefault("ejemplaresPrestados", 0))}
            });

            // Seccion: Clientes
            filaActual++;
            filaActual = agregarSeccion(hoja, filaActual, "Clientes", estiloSeccion, estiloEtiqueta, estiloValor, new String[][]{
                    {"Total Clientes", String.valueOf(datos.getOrDefault("totalClientes", 0))},
                    {"Clientes Activos", String.valueOf(datos.getOrDefault("clientesActivos", 0))},
                    {"Clientes Inactivos", String.valueOf(datos.getOrDefault("clientesInactivos", 0))}
            });

            // Seccion: Prestamos
            filaActual++;
            filaActual = agregarSeccion(hoja, filaActual, "Prestamos", estiloSeccion, estiloEtiqueta, estiloValor, new String[][]{
                    {"Prestamos Activos", String.valueOf(datos.getOrDefault("prestamosActivos", 0))},
                    {"Prestamos Vencidos", String.valueOf(datos.getOrDefault("prestamosVencidos", 0))},
                    {"Prestamos Hoy", String.valueOf(datos.getOrDefault("prestamosHoy", 0))},
                    {"Devoluciones Hoy", String.valueOf(datos.getOrDefault("devolucionesHoy", 0))}
            });

            // Seccion: Catalogo
            filaActual++;
            agregarSeccion(hoja, filaActual, "Catalogo", estiloSeccion, estiloEtiqueta, estiloValor, new String[][]{
                    {"Total Autores", String.valueOf(datos.getOrDefault("totalAutores", 0))},
                    {"Total Categorias", String.valueOf(datos.getOrDefault("totalCategorias", 0))}
            });

            // Auto-ajustar columnas
            hoja.autoSizeColumn(0);
            hoja.autoSizeColumn(1);

            return escribirBytes(workbook);
        }
    }

    // --- Helpers privados ---

    private int agregarSeccion(Sheet hoja, int filaInicio, String tituloSeccion,
                               CellStyle estiloSeccion, CellStyle estiloEtiqueta,
                               CellStyle estiloValor, String[][] datos) {
        Row filaSeccion = hoja.createRow(filaInicio);
        Cell celdaSeccion = filaSeccion.createCell(0);
        celdaSeccion.setCellValue(tituloSeccion);
        celdaSeccion.setCellStyle(estiloSeccion);
        hoja.addMergedRegion(new CellRangeAddress(filaInicio, filaInicio, 0, 1));

        int filaActual = filaInicio + 1;
        for (String[] dato : datos) {
            Row fila = hoja.createRow(filaActual++);

            Cell celdaEtiqueta = fila.createCell(0);
            celdaEtiqueta.setCellValue(dato[0]);
            celdaEtiqueta.setCellStyle(estiloEtiqueta);

            Cell celdaValor = fila.createCell(1);
            celdaValor.setCellValue(dato[1]);
            celdaValor.setCellStyle(estiloValor);
        }

        return filaActual;
    }

    private byte[] escribirBytes(XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        return baos.toByteArray();
    }

    private CellStyle crearEstiloTitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 16);
        fuente.setColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }

    private CellStyle crearEstiloSubtitulo(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setFontHeightInPoints((short) 10);
        fuente.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }

    private CellStyle crearEstiloEncabezado(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 10);
        fuente.setColor(IndexedColors.WHITE.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        return estilo;
    }

    private CellStyle crearEstiloCelda(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setFontHeightInPoints((short) 10);
        estilo.setFont(fuente);
        estilo.setBorderBottom(BorderStyle.THIN);
        estilo.setBorderTop(BorderStyle.THIN);
        estilo.setBorderLeft(BorderStyle.THIN);
        estilo.setBorderRight(BorderStyle.THIN);
        estilo.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        return estilo;
    }

    private CellStyle crearEstiloCeldaCentrada(Workbook workbook) {
        CellStyle estilo = crearEstiloCelda(workbook);
        estilo.setAlignment(HorizontalAlignment.CENTER);
        return estilo;
    }

    private CellStyle crearEstiloDanger(Workbook workbook) {
        CellStyle estilo = crearEstiloCeldaCentrada(workbook);
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 10);
        fuente.setColor(IndexedColors.RED.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }

    private CellStyle crearEstiloResumen(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 12);
        fuente.setColor(IndexedColors.DARK_BLUE.getIndex());
        estilo.setFont(fuente);
        return estilo;
    }

    private CellStyle crearEstiloSeccion(Workbook workbook) {
        CellStyle estilo = workbook.createCellStyle();
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 12);
        fuente.setColor(IndexedColors.TEAL.getIndex());
        estilo.setFont(fuente);
        estilo.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        estilo.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return estilo;
    }

    private CellStyle crearEstiloValor(Workbook workbook) {
        CellStyle estilo = crearEstiloCelda(workbook);
        Font fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeightInPoints((short) 10);
        estilo.setFont(fuente);
        estilo.setAlignment(HorizontalAlignment.RIGHT);
        return estilo;
    }
}
