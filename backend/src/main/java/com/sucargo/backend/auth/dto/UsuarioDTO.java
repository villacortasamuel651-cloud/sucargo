package com.sucargo.backend.auth.dto;

public record UsuarioDTO(
        String id,
        String nombre,
        String email,
        String rol,
        String estado,
        String empresaId
) {}