package com.sucargo.backend.usuario.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.usuario.dto.*;
import com.sucargo.backend.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(usuarioService.listar(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(usuarioService.obtener(id, empresaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> editarPerfil(
            @PathVariable String id, @Valid @RequestBody EditarPerfilRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(usuarioService.editarPerfil(id, request, empresaId));
    }

    @PutMapping("/{id}/rol")
    public ResponseEntity<RolResponse> cambiarRol(
            @PathVariable String id, @Valid @RequestBody CambiarRolRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(usuarioService.cambiarRol(id, request, empresaId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<EstadoResponse> cambiarEstado(
            @PathVariable String id, @Valid @RequestBody CambiarEstadoRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(usuarioService.cambiarEstado(id, request, empresaId));
    }

    @PostMapping("/{id}/cerrar-sesiones")
    public ResponseEntity<Void> cerrarSesiones(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        usuarioService.cerrarSesiones(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        UsuarioResponse creado = usuarioService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    // --- Helper para leer el empresaId del contexto de seguridad ---

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();
        return details.empresaId();
    }
}