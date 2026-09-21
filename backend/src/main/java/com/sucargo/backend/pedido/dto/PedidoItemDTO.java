package com.sucargo.backend.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PedidoItemDTO(
        @NotBlank String productoId,
        @NotNull @Min(value = 1, message = "La cantidad debe ser mayor que cero")
        Integer cantidad
) {}