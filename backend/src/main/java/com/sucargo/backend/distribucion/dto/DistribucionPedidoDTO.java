package com.sucargo.backend.distribucion.dto;

public record DistribucionPedidoDTO(
    String pedidoId,
    String codigo,
    String cliente,
    Integer secuencia
) {}