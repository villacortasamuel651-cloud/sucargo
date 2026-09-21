package com.sucargo.backend.distribucion.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.distribucion.dto.*;
import com.sucargo.backend.distribucion.service.DistribucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/distribuciones")
@RequiredArgsConstructor
public class DistribucionController {

    private final DistribucionService distribucionService;

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();

        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();

        return details.empresaId();
    }

    @GetMapping
    public ResponseEntity<List<DistribucionListResponse>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return ResponseEntity.ok(
                distribucionService.listar(
                        extraerEmpresaIdDelToken(),
                        estado,
                        fecha
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DistribucionDetailResponse> obtener(
            @PathVariable String id) {

        return ResponseEntity.ok(
                distribucionService.obtener(
                        id,
                        extraerEmpresaIdDelToken()
                )
        );
    }

    @PostMapping
    public ResponseEntity<DistribucionDetailResponse> crear(
            @RequestBody CrearDistribucionRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        distribucionService.crear(
                                request,
                                extraerEmpresaIdDelToken()
                        )
                );
    }

    @PostMapping("/{id}/pedidos")
    public ResponseEntity<DistribucionPedidoDTO> agregarPedido(
            @PathVariable String id,
            @RequestBody AgregarPedidoRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        distribucionService.agregarPedido(
                                id,
                                request,
                                extraerEmpresaIdDelToken()
                        )
                );
    }

    @DeleteMapping("/{id}/pedidos/{pedidoId}")
    public ResponseEntity<Void> quitarPedido(
            @PathVariable String id,
            @PathVariable String pedidoId) {

        distribucionService.quitarPedido(
                id,
                pedidoId,
                extraerEmpresaIdDelToken()
        );

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/pedidos/{pedidoId}/secuencia")
    public ResponseEntity<DistribucionPedidoDTO> reordenarSecuencia(
            @PathVariable String id,
            @PathVariable String pedidoId,
            @RequestBody ReordenarSecuenciaRequest request) {

        return ResponseEntity.ok(
                distribucionService.reordenarSecuencia(
                        id,
                        pedidoId,
                        request,
                        extraerEmpresaIdDelToken()
                )
        );
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<DistribucionDetailResponse> confirmar(
            @PathVariable String id) {

        return ResponseEntity.ok(
                distribucionService.confirmar(
                        id,
                        extraerEmpresaIdDelToken()
                )
        );
    }

    @DeleteMapping("/{id}")
        public ResponseEntity<Void> eliminar(@PathVariable String id) {
        distribucionService.eliminar(
            id,
            extraerEmpresaIdDelToken()
        );

        return ResponseEntity.noContent().build();
        }
}