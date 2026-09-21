package com.sucargo.backend.producto.dto;

import java.math.BigDecimal;

public record ProductoResponse(
        String id,
        String sku,
        String nombre,
        String descripcion,
        CategoriaProductoResponse categoria, // objeto anidado completo, no solo el id
        String unidadMedida,
        BigDecimal peso,
        Integer stockMinimo,
        String estado
) {}