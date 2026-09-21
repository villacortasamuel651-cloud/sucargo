package com.sucargo.backend.auth.service;

import com.sucargo.backend.auth.dto.*;
import com.sucargo.backend.auth.entity.RefreshToken;
import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.auth.repository.RefreshTokenRepository;
import com.sucargo.backend.auth.repository.UsuarioRepository;
import com.sucargo.backend.shared.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private static final long REFRESH_TOKEN_EXPIRATION_DAYS = 7;

    @SuppressWarnings("null")
    public UsuarioDTO register(RegisterRequest request, String empresaIdDelToken) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID().toString())
                .empresaId(empresaIdDelToken) // viene del JWT de quien llama, NUNCA del body
                .nombre(request.nombre())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(Usuario.Rol.valueOf(request.rol()))
                .estado(Usuario.Estado.ACTIVO)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);

        return toDTO(guardado);
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new CredencialesInvalidasException());
        System.out.println("USUARIO ENCONTRADO: " + usuario.getEmail());
        System.out.println("PASSWORD INGRESADA: " + request.password());
        System.out.println("HASH BD: " + usuario.getPasswordHash());
        System.out.println("PASSWORD MATCH: " +
        passwordEncoder.matches(request.password(), usuario.getPasswordHash()));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        if (usuario.getEstado() == Usuario.Estado.INACTIVO) {
            throw new UsuarioInactivoException();
        }

        String accessToken = jwtUtil.generateAccessToken(
                usuario.getId(), usuario.getEmpresaId(), usuario.getRol().name()
        );
        String refreshTokenValue = generarYGuardarRefreshToken(usuario.getId());

        return new LoginResponse(
                accessToken,
                refreshTokenValue,
                900, // 15 min, mismo valor que jwt.access-token-expiration-ms / 1000
                toDTO(usuario)
        );
    }

    @SuppressWarnings("null")
    public RefreshResponse refresh(RefreshRequest request) {
        RefreshToken tokenGuardado = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new TokenInvalidoException());

        if (tokenGuardado.getRevocado() || tokenGuardado.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new TokenInvalidoException();
        }

        Usuario usuario = usuarioRepository.findById(tokenGuardado.getUsuarioId())
                .orElseThrow(() -> new TokenInvalidoException());

        // Rotación: revocamos el viejo y creamos uno nuevo
        tokenGuardado.setRevocado(true);
        refreshTokenRepository.save(tokenGuardado);

        String nuevoAccessToken = jwtUtil.generateAccessToken(
                usuario.getId(), usuario.getEmpresaId(), usuario.getRol().name()
        );
        String nuevoRefreshToken = generarYGuardarRefreshToken(usuario.getId());

        return new RefreshResponse(nuevoAccessToken, nuevoRefreshToken, 900);
    }

    public void logout(RefreshRequest request) {
        RefreshToken tokenGuardado = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new TokenInvalidoException());

        tokenGuardado.setRevocado(true);
        refreshTokenRepository.save(tokenGuardado);
    }

    @SuppressWarnings("null")
    public UsuarioDTO me(String userId) {
        Usuario usuario = usuarioRepository.findById(userId)
                .orElseThrow(() -> new TokenInvalidoException());

        return toDTO(usuario);
    }

    @SuppressWarnings("null")
    private String generarYGuardarRefreshToken(String usuarioId) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .id(UUID.randomUUID().toString())
                .usuarioId(usuarioId)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(REFRESH_TOKEN_EXPIRATION_DAYS))
                .revocado(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }

    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getEstado().name(),
                usuario.getEmpresaId()
        );
    }
}