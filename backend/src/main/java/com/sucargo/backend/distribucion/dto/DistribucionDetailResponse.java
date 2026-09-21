package com.sucargo.backend.distribucion.dto;

import java.time.LocalDate;
import java.util.List;

public record DistribucionDetailResponse(
    String id,
    String codigo,
    TransportistaResumenDTO transportista,
    LocalDate fecha,
    String estado,
    List<DistribucionPedidoDTO> pedidos
) {}