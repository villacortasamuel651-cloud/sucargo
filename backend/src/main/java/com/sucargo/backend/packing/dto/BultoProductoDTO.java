package com.sucargo.backend.packing.dto;

public record BultoProductoDTO(
        String productoId,
        String sku,
        String nombre,
        Integer cantidad
) {}