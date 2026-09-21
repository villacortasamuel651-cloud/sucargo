package com.sucargo.backend.picking.dto;

public record RegistrarLineaRequest(
    Integer cantidadRecolectada,
    String incidencia
) {}