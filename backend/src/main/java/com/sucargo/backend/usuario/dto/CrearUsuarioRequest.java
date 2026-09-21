package com.sucargo.backend.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CrearUsuarioRequest(
        @NotBlank String nombre,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String rol
) {}