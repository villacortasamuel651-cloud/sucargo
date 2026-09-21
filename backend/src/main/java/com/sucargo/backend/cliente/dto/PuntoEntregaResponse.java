package com.sucargo.backend.cliente.dto;

public record PuntoEntregaResponse(
        String id,
        String direccion,
        String distrito,
        String referencia,
        String estado
) {}
