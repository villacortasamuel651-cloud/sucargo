package com.sucargo.backend.packing.dto;

import jakarta.validation.constraints.NotBlank;

public record CrearBultoRequest(
        @NotBlank String pedidoId
) {}