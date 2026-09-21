package com.sucargo.backend.ordentransporte.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.ordentransporte.dto.*;
import com.sucargo.backend.ordentransporte.service.OrdenTransporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class OrdenTransporteController {

    private final OrdenTransporteService ordenTransporteService;

    public OrdenTransporteController(
            OrdenTransporteService ordenTransporteService) {

        this.ordenTransporteService = ordenTransporteService;
    }

    // =========================================================
    // LISTAR ÓRDENES
    // =========================================================

    @GetMapping("/ordenes-transporte")
    public ResponseEntity<List<OrdenTransporteListResponse>> listar(
            @RequestParam(required = false) String distribucionId,
            @RequestParam(required = false) String estado) {

        String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity.ok(
                ordenTransporteService.listarOrdenes(
                        distribucionId,
                        estado,
                        empresaId
                )
        );
    }

    // =========================================================
    // OBTENER ORDEN
    // =========================================================

    @GetMapping("/ordenes-transporte/{id}")
    public ResponseEntity<OrdenTransporteDetailResponse> obtenerPorId(
            @PathVariable String id) {

        String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity.ok(
                ordenTransporteService.obtenerPorId(
                        id,
                        empresaId
                )
        );
    }

    // =========================================================
    // GENERAR ÓRDENES
    // =========================================================

    @PostMapping("/distribuciones/{id}/generar-ordenes")
    public ResponseEntity<List<OrdenTransporteDetailResponse>> generarOrdenes(
            @PathVariable String id,
            @RequestBody(required = false)
            GenerarOrdenesRequest request) {

        String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ordenTransporteService.generarOrdenes(
                                id,
                                request,
                                empresaId
                        )
                );
    }

    // =========================================================
    // ANULAR ORDEN
    // =========================================================

    @PostMapping("/ordenes-transporte/{id}/anular")
    public ResponseEntity<OrdenTransporteDetailResponse> anular(
            @PathVariable String id) {

        String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity.ok(
                ordenTransporteService.anularOrden(
                        id,
                        empresaId
                )
        );
    }

    // =========================================================
    // JWT
    // =========================================================

    private String extraerEmpresaIdDelToken() {

        var auth =
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication();

        var details =
                (JwtAuthFilter.JwtUserDetails)
                        auth.getDetails();

        return details.empresaId();
    }
}