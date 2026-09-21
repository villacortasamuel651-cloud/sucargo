package com.sucargo.backend.picking.dto;

public record CompletarPickingResponse(
    String id,
    String estado,
    String pedidoEstado
) {}