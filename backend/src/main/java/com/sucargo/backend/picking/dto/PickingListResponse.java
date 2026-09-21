package com.sucargo.backend.picking.dto;

import java.time.LocalDateTime;

public record PickingListResponse(
    String id,
    PedidoResumenDTO pedido,
    String estado,
    String usuario,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin
) {}