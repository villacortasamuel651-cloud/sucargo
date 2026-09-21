package com.sucargo.backend.producto.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.producto.dto.ProductoRequest;
import com.sucargo.backend.producto.dto.ProductoResponse;
import com.sucargo.backend.producto.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoResponse>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoriaId) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(productoService.listar(empresaId, estado, q, categoriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponse> obtener(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(productoService.obtener(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        ProductoResponse creado = productoService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponse> editar(
            @PathVariable String id, @Valid @RequestBody ProductoRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(productoService.editar(id, request, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        productoService.desactivar(id, empresaId);
        return ResponseEntity.noContent().build();
    }

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();
        return details.empresaId();
    }
}