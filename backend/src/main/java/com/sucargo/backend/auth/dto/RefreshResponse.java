package com.sucargo.backend.auth.dto;

public record RefreshResponse(
        String accessToken,
        String refreshToken,
        long expiresIn
) {}