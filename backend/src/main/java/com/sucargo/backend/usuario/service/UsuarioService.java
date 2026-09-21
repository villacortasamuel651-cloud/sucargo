package com.sucargo.backend.usuario.service;

import com.sucargo.backend.auth.entity.RefreshToken;
import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.auth.repository.RefreshTokenRepository;
import com.sucargo.backend.auth.repository.UsuarioRepository;
import com.sucargo.backend.usuario.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UsuarioResponse> listar(String empresaId) {
        return usuarioRepository.findByEmpresaId(empresaId).stream()
                .map(u -> UsuarioResponse.paraLista(
                        u.getId(), u.getNombre(), u.getEmail(), u.getRol().name(),
                        u.getEstado().name(), u.getCreatedAt(), u.getUltimoLogin()))
                .toList();
    }

    public UsuarioResponse obtener(String id, String empresaId) {
        Usuario usuario = buscarUsuarioDeEmpresa(id, empresaId);
        return toDetalleDTO(usuario);
    }

    @Transactional
    public UsuarioResponse editarPerfil(String id, EditarPerfilRequest request, String empresaId) {
        Usuario usuario = buscarUsuarioDeEmpresa(id, empresaId);
        usuario.setNombre(request.nombre());
        Usuario actualizado = usuarioRepository.save(usuario);
        return toDetalleDTO(actualizado);
    }

    @Transactional
    public RolResponse cambiarRol(String id, CambiarRolRequest request, String empresaId) {
        Usuario usuario = buscarUsuarioDeEmpresa(id, empresaId);

        if ("ADMIN_SUC".equals(request.rol())) {
            throw new RolNoPermitidoException();
        }

        usuario.setRol(Usuario.Rol.valueOf(request.rol()));
        Usuario actualizado = usuarioRepository.save(usuario);

        return new RolResponse(actualizado.getId(), actualizado.getRol().name());
    }

    @Transactional
    public EstadoResponse cambiarEstado(String id, CambiarEstadoRequest request, String empresaId) {
        Usuario usuario = buscarUsuarioDeEmpresa(id, empresaId);

        Usuario.Estado nuevoEstado = Usuario.Estado.valueOf(request.estado());
        usuario.setEstado(nuevoEstado);
        Usuario actualizado = usuarioRepository.save(usuario);

        // Si se desactiva, revocar todas sus sesiones activas
        if (nuevoEstado == Usuario.Estado.INACTIVO) {
            revocarTodasLasSesiones(usuario.getId());
        }

        return new EstadoResponse(actualizado.getId(), actualizado.getEstado().name());
    }

    @Transactional
    public void cerrarSesiones(String id, String empresaId) {
        buscarUsuarioDeEmpresa(id, empresaId); // valida que el usuario sea de la empresa
        revocarTodasLasSesiones(id);
    }

    @SuppressWarnings("null")
    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest request, String empresaId) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if ("ADMIN_SUC".equals(request.rol())) {
            throw new RolNoPermitidoException();
        }

        Usuario usuario = Usuario.builder()
                .empresaId(empresaId)
                .nombre(request.nombre())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .rol(Usuario.Rol.valueOf(request.rol()))
                .estado(Usuario.Estado.ACTIVO)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return toDetalleDTO(guardado);
    }

    // --- Helpers privados ---

    private Usuario buscarUsuarioDeEmpresa(String id, String empresaId) {
        return usuarioRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(UsuarioNoEncontradoException::new);
    }

    private void revocarTodasLasSesiones(String usuarioId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUsuarioId(usuarioId);
        tokens.forEach(t -> t.setRevocado(true));
        refreshTokenRepository.saveAll(tokens);
    }

    private UsuarioResponse toDetalleDTO(Usuario usuario) {
        int sesionesActivas = (int) refreshTokenRepository
                .countByUsuarioIdAndRevocadoFalseAndExpiresAtAfter(
                        usuario.getId(), LocalDateTime.now());

        return UsuarioResponse.paraDetalle(
                usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getRol().name(), usuario.getEstado().name(),
                usuario.getCreatedAt(), usuario.getUltimoLogin(), sesionesActivas);
    }
}