package com.biblioteca.bibliotech.mapper;

import com.biblioteca.bibliotech.dto.request.AutorRequest;
import com.biblioteca.bibliotech.dto.response.AutorResponse;
import com.biblioteca.bibliotech.entity.Autor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper para conversión entre Autor Entity y DTOs.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AutorMapper {
    
    /**
     * Convierte Request a Entity (para crear).
     */
    Autor toEntity(AutorRequest request);
    
    /**
     * Convierte Entity a Response.
     * El campo cantidadLibros se setea manualmente en el Service.
     */
    @Mapping(target = "cantidadLibros", ignore = true)
    AutorResponse toResponse(Autor autor);
    
    /**
     * Convierte lista de Entity a lista de Response.
     */
    List<AutorResponse> toResponseList(List<Autor> autores);
    
    /**
     * Actualiza Entity existente con datos del Request (para editar).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "libros", ignore = true)
    void updateEntityFromRequest(AutorRequest request, @MappingTarget Autor autor);
}
