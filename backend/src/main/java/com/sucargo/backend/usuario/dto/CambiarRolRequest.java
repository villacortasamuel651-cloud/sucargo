package com.sucargo.backend.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record CambiarRolRequest(
        @NotBlank String rol
) {}