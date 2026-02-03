package com.biblioteca.bibliotech.mapper;

import com.biblioteca.bibliotech.dto.request.ClienteRequest;
import com.biblioteca.bibliotech.dto.response.ClienteResponse;
import com.biblioteca.bibliotech.entity.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

/**
 * Mapper para conversión entre Cliente Entity y DTOs.
 */
@Mapper(componentModel = "spring", 
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ClienteMapper {
    
    /**
     * Convierte Request a Entity (para crear).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "prestamos", ignore = true)
    Cliente toEntity(ClienteRequest request);
    
    /**
     * Convierte Entity a Response.
     * Los campos calculados se setean manualmente en el Service.
     */
    @Mapping(target = "nombreCompleto", expression = "java(cliente.getNombre() + \" \" + cliente.getApellido())")
    @Mapping(target = "estadoDescripcion", expression = "java(cliente.getActivo() ? \"Activo\" : \"Inactivo\")")
    @Mapping(target = "prestamosActivos", ignore = true)
    @Mapping(target = "prestamosVencidos", ignore = true)
    @Mapping(target = "totalPrestamos", ignore = true)
    @Mapping(target = "puedePrestar", ignore = true)
    ClienteResponse toResponse(Cliente cliente);
    
    /**
     * Convierte lista de Entity a lista de Response.
     */
    List<ClienteResponse> toResponseList(List<Cliente> clientes);
    
    /**
     * Actualiza Entity existente con datos del Request (para editar).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaRegistro", ignore = true)
    @Mapping(target = "prestamos", ignore = true)
    void updateEntityFromRequest(ClienteRequest request, @MappingTarget Cliente cliente);
}
