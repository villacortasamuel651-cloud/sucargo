package com.sucargo.backend.inventario.dto;

public record ResultadoMovimientoResponse(
        String inventarioId,
        Integer stockFisico,
        Integer stockReservado,
        Integer stockDisponible
) {}