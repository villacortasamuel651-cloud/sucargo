package com.sucargo.backend.auth.repository;

import com.sucargo.backend.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    Optional<RefreshToken> findByToken(String token);

    // NUEVO: todos los refresh tokens de un usuario (para revocarlos todos)
    List<RefreshToken> findByUsuarioId(String usuarioId);

    // NUEVO: cuenta las sesiones activas (no revocadas y no expiradas)
    // para el campo "sesionesActivas" del contrato
    long countByUsuarioIdAndRevocadoFalseAndExpiresAtAfter(String usuarioId, LocalDateTime ahora);
}