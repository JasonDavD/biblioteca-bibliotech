package com.biblioteca.bibliotech.mapper;

import com.biblioteca.bibliotech.dto.request.LibroRequest;
import com.biblioteca.bibliotech.dto.response.LibroResponse;
import com.biblioteca.bibliotech.entity.Libro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper para conversión entre Libro Entity y DTOs.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LibroMapper {
    
    /**
     * Convierte Request a Entity (para crear).
     * Las relaciones (autor, categoria) se manejan en el Service.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cantidadDisponible", source = "cantidadTotal")
    @Mapping(target = "ultimaActualizacion", ignore = true)
    @Mapping(target = "prestamos", ignore = true)
    Libro toEntity(LibroRequest request);
    
    /**
     * Convierte Entity a Response.
     */
    @Mapping(target = "idAutor", source = "autor.id")
    @Mapping(target = "nombreAutor", source = "autor.nombre")
    @Mapping(target = "idCategoria", source = "categoria.id")
    @Mapping(target = "nombreCategoria", source = "categoria.nombre")
    @Mapping(target = "disponible", expression = "java(libro.getCantidadDisponible() > 0)")
    @Mapping(target = "cantidadPrestada", expression = "java(libro.getCantidadTotal() - libro.getCantidadDisponible())")
    LibroResponse toResponse(Libro libro);
    
    /**
     * Convierte lista de Entity a lista de Response.
     */
    List<LibroResponse> toResponseList(List<Libro> libros);
    
    /**
     * Actualiza Entity existente con datos del Request (para editar).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "autor", ignore = true)
    @Mapping(target = "categoria", ignore = true)
    @Mapping(target = "cantidadDisponible", ignore = true)
    @Mapping(target = "ultimaActualizacion", ignore = true)
    @Mapping(target = "prestamos", ignore = true)
    void updateEntityFromRequest(LibroRequest request, @MappingTarget Libro libro);
}
