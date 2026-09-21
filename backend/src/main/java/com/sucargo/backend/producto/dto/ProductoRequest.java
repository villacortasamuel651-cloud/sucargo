package com.sucargo.backend.producto.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record ProductoRequest(
        @NotBlank String sku,
        @NotBlank String nombre,
        String descripcion,
        String categoriaId, // opcional: puede venir null, el producto queda sin categoría
        @NotBlank String unidadMedida,
        BigDecimal peso,
        @Min(value = 0, message = "El stock mínimo no puede ser negativo")
        Integer stockMinimo
) {}