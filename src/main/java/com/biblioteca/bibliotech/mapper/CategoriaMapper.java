package com.biblioteca.bibliotech.mapper;

import com.biblioteca.bibliotech.dto.request.CategoriaRequest;
import com.biblioteca.bibliotech.dto.response.CategoriaResponse;
import com.biblioteca.bibliotech.entity.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper para conversión entre Categoria Entity y DTOs.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CategoriaMapper {
    
    /**
     * Convierte Request a Entity (para crear).
     */
    Categoria toEntity(CategoriaRequest request);
    
    /**
     * Convierte Entity a Response.
     * El campo cantidadLibros se setea manualmente en el Service.
     */
    @Mapping(target = "cantidadLibros", ignore = true)
    CategoriaResponse toResponse(Categoria categoria);
    
    /**
     * Convierte lista de Entity a lista de Response.
     */
    List<CategoriaResponse> toResponseList(List<Categoria> categorias);
    
    /**
     * Actualiza Entity existente con datos del Request (para editar).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "libros", ignore = true)
    void updateEntityFromRequest(CategoriaRequest request, @MappingTarget Categoria categoria);
}
