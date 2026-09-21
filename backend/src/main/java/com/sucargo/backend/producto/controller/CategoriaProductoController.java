package com.sucargo.backend.producto.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.producto.dto.CategoriaProductoRequest;
import com.sucargo.backend.producto.dto.CategoriaProductoResponse;
import com.sucargo.backend.producto.service.CategoriaProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categorias-producto")
@RequiredArgsConstructor
public class CategoriaProductoController {

    private final CategoriaProductoService categoriaProductoService;

    @GetMapping
    public ResponseEntity<List<CategoriaProductoResponse>> listar() {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(categoriaProductoService.listar(empresaId));
    }

    @PostMapping
    public ResponseEntity<CategoriaProductoResponse> crear(
            @Valid @RequestBody CategoriaProductoRequest request) {
        String empresaId = extraerEmpresaIdDelToken();
        CategoriaProductoResponse creada = categoriaProductoService.crear(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();
        return details.empresaId();
    }
}