package com.sucargo.backend.distribucion.dto;

import java.time.LocalDate;

public record CrearDistribucionRequest(
    String transportistaId,
    LocalDate fecha
) {}