package com.sucargo.backend.dashboard.dto;

import java.util.List;

public record ResumenDashboardResponse(
        int pedidos,
        int picking,
        int listosDespacho,
        int productosStockBajo,
        List<PedidoRecienteDTO> pedidosRecientes,
        List<AlertaDTO> alertas
) {}