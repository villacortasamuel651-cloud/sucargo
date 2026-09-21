package com.sucargo.backend.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClienteRequest(
        @NotBlank String razonSocial,

        @NotBlank
        @Pattern(regexp = "\\d{8}|\\d{11}", message = "El RUC/DNI debe tener 8 dígitos (DNI) u 11 dígitos (RUC)")
        String rucDni,

        String telefono
) {}