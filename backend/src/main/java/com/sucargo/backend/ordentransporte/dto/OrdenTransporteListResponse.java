package com.sucargo.backend.ordentransporte.dto;

import java.math.BigDecimal;

public record OrdenTransporteListResponse(
        String id,
        String numero,
        PedidoResumenDTO pedido,
        String destino,
        Integer totalBultos,
        BigDecimal pesoTotal,
        String estado
) {
    public record PedidoResumenDTO(String id, String codigo) {}
}