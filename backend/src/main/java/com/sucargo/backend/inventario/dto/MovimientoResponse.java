package com.sucargo.backend.inventario.dto;

import java.time.LocalDateTime;

public record MovimientoResponse(
        String id,
        String tipo,
        Integer cantidad,
        String motivo,
        String usuario, // nombre del usuario, no el id
        LocalDateTime fecha
) {}