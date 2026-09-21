package com.sucargo.backend.packing.dto;

public record CompletarPackingResponse(
        String pedidoId,
        String estado,
        Integer totalBultos
) {}