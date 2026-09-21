package com.sucargo.backend.inventario.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.inventario.dto.*;
import com.sucargo.backend.inventario.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping
    public ResponseEntity<List<InventarioResponse>> listar(
            @RequestParam(required = false) String almacenId,
            @RequestParam(required = false) String productoId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean stockBajo) {

        String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity.ok(
                inventarioService.listar(
                        empresaId,
                        almacenId,
                        productoId,
                        q,
                        stockBajo
                )
        );
    }

    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<MovimientoResponse>> movimientos(
            @PathVariable String id) {

        String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity.ok(
                inventarioService.historial(id, empresaId)
        );
    }

    @PostMapping("/movimientos")
    public ResponseEntity<ResultadoMovimientoResponse> registrarMovimiento(
            @Valid @RequestBody MovimientoRequest request) {

        String empresaId = extraerEmpresaIdDelToken();
        String usuarioId = extraerUserIdDelToken();
        String rolUsuario = extraerRolDelToken();

        return ResponseEntity.ok(
                inventarioService.registrarMovimiento(
                        request,
                        usuarioId,
                        rolUsuario,
                        empresaId
                )
        );
    }

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();

        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();

        return details.empresaId();
    }

    private String extraerUserIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();

        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();

        return details.userId();
    }

    private String extraerRolDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();

        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();

        return details.rol();
    }
}