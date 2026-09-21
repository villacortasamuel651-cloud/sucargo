package com.sucargo.backend.almacen.dto;

import jakarta.validation.constraints.NotBlank;

public record AlmacenRequest(
        @NotBlank String nombre,
        String direccion
) {}