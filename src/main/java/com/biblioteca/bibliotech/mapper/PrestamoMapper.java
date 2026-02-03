package com.biblioteca.bibliotech.mapper;

import com.biblioteca.bibliotech.dto.request.PrestamoRequest;
import com.biblioteca.bibliotech.dto.response.PrestamoResponse;
import com.biblioteca.bibliotech.entity.Prestamo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Mapper para conversión entre Prestamo Entity y DTOs.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {LocalDate.class, ChronoUnit.class})
public interface PrestamoMapper {
    
    /**
     * Convierte Request a Entity (para crear).
     * Las relaciones (libro, cliente) y fechaPrestamo se manejan en el Service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "libro", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "fechaPrestamo", ignore = true)
    @Mapping(target = "fechaDevolucionReal", ignore = true)
    @Mapping(target = "estado", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    Prestamo toEntity(PrestamoRequest request);
    
    /**
     * Convierte Entity a Response con todos los campos calculados.
     */
    @Mapping(target = "idLibro", source = "libro.id")
    @Mapping(target = "tituloLibro", source = "libro.titulo")
    @Mapping(target = "isbnLibro", source = "libro.isbn")
    @Mapping(target = "idCliente", source = "cliente.id")
    @Mapping(target = "nombreCompletoCliente", expression = "java(prestamo.getCliente().getNombre() + \" \" + prestamo.getCliente().getApellido())")
    @Mapping(target = "dniCliente", source = "cliente.dni")
    @Mapping(target = "estadoDescripcion", expression = "java(prestamo.getEstado().getDescripcion())")
    @Mapping(target = "diasRestantes", expression = "java(calcularDiasRestantes(prestamo))")
    @Mapping(target = "diasRetraso", expression = "java(calcularDiasRetraso(prestamo))")
    @Mapping(target = "estaVencido", expression = "java(prestamo.estaVencido())")
    PrestamoResponse toResponse(Prestamo prestamo);
    
    /**
     * Convierte lista de Entity a lista de Response.
     */
    List<PrestamoResponse> toResponseList(List<Prestamo> prestamos);
    
    /**
     * Calcula los días restantes para la devolución.
     * Retorna negativo si ya pasó la fecha.
     */
    default Long calcularDiasRestantes(Prestamo prestamo) {
        if (prestamo.getFechaDevolucionReal() != null) {
            return 0L; // Ya fue devuelto
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), prestamo.getFechaDevolucionEsperada());
    }
    
    /**
     * Calcula los días de retraso.
     * Retorna 0 si no está vencido.
     */
    default Long calcularDiasRetraso(Prestamo prestamo) {
        if (prestamo.getFechaDevolucionReal() != null) {
            // Si ya fue devuelto, calcular retraso basado en fecha real
            if (prestamo.getFechaDevolucionReal().isAfter(prestamo.getFechaDevolucionEsperada())) {
                return ChronoUnit.DAYS.between(prestamo.getFechaDevolucionEsperada(), prestamo.getFechaDevolucionReal());
            }
            return 0L;
        }
        // Si no ha sido devuelto, calcular desde fecha esperada hasta hoy
        if (LocalDate.now().isAfter(prestamo.getFechaDevolucionEsperada())) {
            return ChronoUnit.DAYS.between(prestamo.getFechaDevolucionEsperada(), LocalDate.now());
        }
        return 0L;
    }
}
