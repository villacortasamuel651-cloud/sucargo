package com.sucargo.backend.inventario.dto;

public record ProductoResumenDTO(
        String id,
        String sku,
        String nombre,
        Integer stockMinimo
) {}