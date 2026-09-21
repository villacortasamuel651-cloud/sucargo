package com.sucargo.backend.dashboard.dto;

public record PedidoRecienteDTO(
        String id,
        String codigo,
        String cliente,
        String fecha,
        String estado,
        String prioridad
) {}
