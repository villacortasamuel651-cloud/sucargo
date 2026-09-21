package com.sucargo.backend.transportista.dto;

public record TransportistaRequest(
    String razonSocial,
    String ruc,
    String contacto,
    String telefono,
    String correo,
    String tipoServicio
) {}