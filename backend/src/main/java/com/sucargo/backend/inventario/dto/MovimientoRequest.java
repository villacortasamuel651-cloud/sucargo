package com.sucargo.backend.inventario.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MovimientoRequest(

        @NotBlank
        String productoId,

        @NotBlank
        String almacenId,

        @NotBlank
        String tipo,

        @NotNull
        @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        Integer cantidad,

        String motivo

) {}