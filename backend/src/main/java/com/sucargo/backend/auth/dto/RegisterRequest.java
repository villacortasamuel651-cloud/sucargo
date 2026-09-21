package com.sucargo.backend.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String nombre,
        @NotBlank @jakarta.validation.constraints.Email String email,
        @NotBlank String password,
        @NotBlank String rol
) {}