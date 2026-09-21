package com.sucargo.backend.picking.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PickingDetailResponse(
    String id,
    PedidoResumenDTO pedido,
    String estado,
    String usuario,
    LocalDateTime fechaInicio,
    LocalDateTime fechaFin,
    List<PickingLineaDTO> lineas
) {}