package com.sucargo.backend.cliente.controller;

import com.sucargo.backend.cliente.dto.*;
import com.sucargo.backend.cliente.service.ClienteService;
import com.sucargo.backend.config.JwtAuthFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String q) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(clienteService.listar(empresaId, estado, q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(clienteService.obtener(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        ClienteResponse creado = clienteService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> editar(
            @PathVariable String id, @Valid @RequestBody ClienteRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(clienteService.editar(id, request, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        clienteService.desactivar(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/puntos-entrega")
    public ResponseEntity<PuntoEntregaResponse> agregarPuntoEntrega(
            @PathVariable String id, @Valid @RequestBody PuntoEntregaRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        PuntoEntregaResponse creado = clienteService.agregarPuntoEntrega(id, request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{clienteId}/puntos-entrega/{puntoId}")
    public ResponseEntity<PuntoEntregaResponse> editarPuntoEntrega(
            @PathVariable String clienteId, @PathVariable String puntoId,
            @Valid @RequestBody PuntoEntregaRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(
                clienteService.editarPuntoEntrega(clienteId, puntoId, request, empresaId));
    }

    @DeleteMapping("/{clienteId}/puntos-entrega/{puntoId}")
    public ResponseEntity<Void> desactivarPuntoEntrega(
            @PathVariable String clienteId, @PathVariable String puntoId) {
        String empresaId = extraerEmpresaIdDelToken();
        clienteService.desactivarPuntoEntrega(clienteId, puntoId, empresaId);
        return ResponseEntity.noContent().build();
    }

    // --- Helper para leer el empresaId del contexto de seguridad ---

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();
        return details.empresaId();
    }
}