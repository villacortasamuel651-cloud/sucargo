package com.sucargo.backend.cliente.dto;

import jakarta.validation.constraints.NotBlank;

public record PuntoEntregaRequest(
        @NotBlank String direccion,
        String distrito,
        String referencia
) {}