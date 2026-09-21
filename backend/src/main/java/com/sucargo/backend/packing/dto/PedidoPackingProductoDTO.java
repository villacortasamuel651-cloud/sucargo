package com.sucargo.backend.packing.dto;

import java.math.BigDecimal;

public record PedidoPackingProductoDTO(
        String productoId,
        String sku,
        String nombre,
        Integer cantidadSolicitada,
        Integer cantidadRecolectada,
        Integer cantidadEmpaquetada,
        Integer cantidadPendiente,
        BigDecimal pesoUnitario,
        BigDecimal pesoEstimadoPendiente
) {
}