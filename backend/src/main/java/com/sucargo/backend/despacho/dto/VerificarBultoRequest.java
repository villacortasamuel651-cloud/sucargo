package com.sucargo.backend.despacho.dto;

import jakarta.validation.constraints.NotBlank;

public record VerificarBultoRequest(
        @NotBlank String bultoId
) {}