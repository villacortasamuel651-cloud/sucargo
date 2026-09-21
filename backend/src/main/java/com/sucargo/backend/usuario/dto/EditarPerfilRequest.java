package com.sucargo.backend.usuario.dto;

import jakarta.validation.constraints.NotBlank;

public record EditarPerfilRequest(
        @NotBlank String nombre
) {}