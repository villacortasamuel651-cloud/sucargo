package com.sucargo.backend.distribucion.dto;

import java.time.LocalDate;

public record DistribucionListResponse(
    String id,
    String codigo,
    TransportistaResumenDTO transportista,
    LocalDate fecha,
    String estado,
    int totalPedidos
) {}