package com.sucargo.backend.picking.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.picking.dto.*;
import com.sucargo.backend.picking.service.PickingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/picking")
@RequiredArgsConstructor
public class PickingController {

    private final PickingService pickingService;

    // Helper para no repetir esto en cada método
    private JwtAuthFilter.JwtUserDetails auth() {
        return (JwtAuthFilter.JwtUserDetails) SecurityContextHolder.getContext()
                .getAuthentication()
                .getDetails();
    }

    @GetMapping
    public ResponseEntity<List<PickingListResponse>> listar(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(pickingService.listar(auth().empresaId(), estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PickingDetailResponse> obtener(@PathVariable String id) {
        return ResponseEntity.ok(pickingService.obtener(id, auth().empresaId()));
    }

    @PostMapping("/generar")
    public ResponseEntity<PickingDetailResponse> generar(
            @RequestBody GenerarPickingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pickingService.generar(request, auth().empresaId()));
    }

    @PostMapping("/{id}/iniciar")
    public ResponseEntity<PickingDetailResponse> iniciar(@PathVariable String id) {
        return ResponseEntity.ok(pickingService.iniciar(id, auth().userId(), auth().empresaId()));
    }

    @PutMapping("/{id}/lineas/{lineaId}")
    public ResponseEntity<PickingLineaDTO> registrarLinea(
            @PathVariable String id,
            @PathVariable String lineaId,
            @RequestBody RegistrarLineaRequest request) {
        return ResponseEntity.ok(pickingService.registrarLinea(id, lineaId, request, auth().empresaId()));
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<CompletarPickingResponse> completar(@PathVariable String id) {
        return ResponseEntity.ok(pickingService.completar(id, auth().empresaId()));
    }
}