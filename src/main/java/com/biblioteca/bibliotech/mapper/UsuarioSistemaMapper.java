package com.biblioteca.bibliotech.mapper;

import com.biblioteca.bibliotech.dto.request.UsuarioSistemaRequest;
import com.biblioteca.bibliotech.dto.response.UsuarioSistemaResponse;
import com.biblioteca.bibliotech.entity.UsuarioSistema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper para conversión entre UsuarioSistema Entity y DTOs.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UsuarioSistemaMapper {
    
    /**
     * Convierte Request a Entity (para crear).
     */
    UsuarioSistema toEntity(UsuarioSistemaRequest request);
    
    /**
     * Convierte Entity a Response.
     */
    @Mapping(target = "rolDescripcion", expression = "java(usuario.getRol().getDescripcion())")
    UsuarioSistemaResponse toResponse(UsuarioSistema usuario);
    
    /**
     * Convierte lista de Entity a lista de Response.
     */
    List<UsuarioSistemaResponse> toResponseList(List<UsuarioSistema> usuarios);
    
    /**
     * Actualiza Entity existente con datos del Request (para editar).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    void updateEntityFromRequest(UsuarioSistemaRequest request, @MappingTarget UsuarioSistema usuario);
}
