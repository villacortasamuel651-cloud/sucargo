package com.sucargo.backend.usuario.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UsuarioResponse(
        String id,
        String nombre,
        String email,
        String rol,
        String estado,
        LocalDateTime createdAt,
        LocalDateTime ultimoLogin,
        Integer sesionesActivas // solo se usa en el detalle (endpoints 2, 3, 7)
) {
    // Para GET /api/usuarios (listado): sin sesionesActivas
    public static UsuarioResponse paraLista(
            String id, String nombre, String email, String rol, String estado,
            LocalDateTime createdAt, LocalDateTime ultimoLogin) {
        return new UsuarioResponse(id, nombre, email, rol, estado, createdAt, ultimoLogin, null);
    }

    // Para GET /{id}, PUT /{id}, POST (perfil completo): con sesionesActivas
    public static UsuarioResponse paraDetalle(
            String id, String nombre, String email, String rol, String estado,
            LocalDateTime createdAt, LocalDateTime ultimoLogin, int sesionesActivas) {
        return new UsuarioResponse(id, nombre, email, rol, estado, createdAt, ultimoLogin, sesionesActivas);
    }
}