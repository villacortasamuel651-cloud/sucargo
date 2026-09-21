package com.sucargo.backend.almacen.controller;

import com.sucargo.backend.almacen.dto.AlmacenRequest;
import com.sucargo.backend.almacen.dto.AlmacenResponse;
import com.sucargo.backend.almacen.service.AlmacenService;
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
@RequestMapping("/api/almacenes")
@RequiredArgsConstructor
public class AlmacenController {

    private final AlmacenService almacenService;

    @GetMapping
    public ResponseEntity<List<AlmacenResponse>> listar() {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(almacenService.listarAlmacenes(empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlmacenResponse> obtenerPorId(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(almacenService.obtenerPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<AlmacenResponse> crear(@Valid @RequestBody AlmacenRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.status(HttpStatus.CREATED).body(almacenService.crear(request, empresaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AlmacenResponse> actualizar(
            @PathVariable String id,
            @Valid @RequestBody AlmacenRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(almacenService.actualizar(id, request, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        almacenService.desactivar(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    // --- Helper para leer el empresaId del contexto de seguridad (mismo patrón que ClienteController) ---

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();
        return details.empresaId();
    }
}