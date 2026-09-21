package com.sucargo.backend.transportista.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.transportista.dto.CambiarEstadoRequest;
import com.sucargo.backend.transportista.dto.TransportistaRequest;
import com.sucargo.backend.transportista.dto.TransportistaResponse;
import com.sucargo.backend.transportista.service.TransportistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transportistas")
@RequiredArgsConstructor
public class TransportistaController {

    private final TransportistaService transportistaService;

    // Obtiene los datos del usuario autenticado desde el JWT
    private JwtAuthFilter.JwtUserDetails auth() {
        return (JwtAuthFilter.JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getDetails();
    }

    @GetMapping
    public ResponseEntity<List<TransportistaResponse>> listar(
            @RequestParam(required = false) String estado) {

        return ResponseEntity.ok(
                transportistaService.listar(auth().empresaId(), estado)
        );
    }

    @PostMapping
    public ResponseEntity<TransportistaResponse> crear(
            @RequestBody TransportistaRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        transportistaService.crear(
                                request,
                                auth().empresaId()
                        )
                );
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransportistaResponse> actualizar(
            @PathVariable String id,
            @RequestBody TransportistaRequest request) {

        return ResponseEntity.ok(
                transportistaService.actualizar(
                        id,
                        request,
                        auth().empresaId()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<TransportistaResponse> desactivar(
            @PathVariable String id) {

        return ResponseEntity.ok(
                transportistaService.desactivar(
                        id,
                        auth().empresaId()
                )
        );
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<TransportistaResponse> cambiarEstado(
            @PathVariable String id,
            @RequestBody CambiarEstadoRequest request) {

        return ResponseEntity.ok(
                transportistaService.cambiarEstado(
                        id,
                        request,
                        auth().empresaId()
                )
        );
    }
}