package com.sucargo.backend.packing.dto;

import java.math.BigDecimal;
import java.util.List;

public record BultoDetailResponse(
        String id,
        String codigo,
        Integer numero,
        Integer totalBultos,
        BigDecimal peso,
        String estado,
        String usuario,
        List<BultoProductoDTO> productos
) {}