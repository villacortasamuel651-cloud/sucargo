package com.sucargo.backend.packing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record RegistrarPesoRequest(
        @NotNull @Positive(message = "El peso debe ser mayor que cero")
        BigDecimal peso
) {}