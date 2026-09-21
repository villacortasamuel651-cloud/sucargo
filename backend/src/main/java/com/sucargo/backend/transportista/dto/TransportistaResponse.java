package com.sucargo.backend.transportista.dto;

public record TransportistaResponse(
    String id,
    String razonSocial,
    String ruc,
    String contacto,
    String telefono,
    String correo,
    String tipoServicio,
    String estado
) {}