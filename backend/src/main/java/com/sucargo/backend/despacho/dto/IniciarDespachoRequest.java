package com.sucargo.backend.despacho.dto;

import jakarta.validation.constraints.NotBlank;

public record IniciarDespachoRequest(
        @NotBlank String pedidoId
) {}