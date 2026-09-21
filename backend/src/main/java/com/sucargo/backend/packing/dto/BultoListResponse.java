package com.sucargo.backend.packing.dto;

public record BultoListResponse(
        String id,
        String codigo,
        Integer numero,
        Integer totalBultos,
        java.math.BigDecimal peso,
        String estado
) {}