package com.sucargo.backend.producto.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoriaProductoRequest(
        @NotBlank String nombre
) {}