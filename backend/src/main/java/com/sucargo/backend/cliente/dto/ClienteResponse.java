package com.sucargo.backend.cliente.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ClienteResponse(
        String id,
        String razonSocial,
        String rucDni,
        String telefono,
        String estado,
        Integer cantidadPuntosEntrega,       
        List<PuntoEntregaResponse> puntosEntrega  
) {
    // Para GET /api/clientes (listado): trae cantidadPuntosEntrega, no la lista completa
    public static ClienteResponse paraLista(
            String id, String razonSocial, String rucDni, String telefono,
            String estado, int cantidadPuntosEntrega) {
        return new ClienteResponse(id, razonSocial, rucDni, telefono, estado, cantidadPuntosEntrega, null);
    }

    // Para GET /{id}, POST, PUT (detalle): trae la lista completa de puntosEntrega
    public static ClienteResponse paraDetalle(
            String id, String razonSocial, String rucDni, String telefono,
            String estado, List<PuntoEntregaResponse> puntosEntrega) {
        return new ClienteResponse(id, razonSocial, rucDni, telefono, estado, null, puntosEntrega);
    }
}