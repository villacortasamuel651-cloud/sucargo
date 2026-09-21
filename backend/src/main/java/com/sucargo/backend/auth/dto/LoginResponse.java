package com.sucargo.backend.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UsuarioDTO usuario
) {}