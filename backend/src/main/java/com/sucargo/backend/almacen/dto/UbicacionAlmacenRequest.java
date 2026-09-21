package com.sucargo.backend.almacen.dto;

import jakarta.validation.constraints.NotBlank;

public record UbicacionAlmacenRequest(
        @NotBlank String codigoZona,
        String descripcion
) {}