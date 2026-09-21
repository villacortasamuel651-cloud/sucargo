package com.sucargo.backend.packing.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.packing.dto.AgregarProductoRequest;
import com.sucargo.backend.packing.dto.BultoDetailResponse;
import com.sucargo.backend.packing.dto.BultoListResponse;
import com.sucargo.backend.packing.dto.BultoProductoDTO;
import com.sucargo.backend.packing.dto.CrearBultoRequest;
import com.sucargo.backend.packing.dto.PedidoPackingProductoDTO;
import com.sucargo.backend.packing.dto.RegistrarPesoRequest;
import com.sucargo.backend.packing.service.BultoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bultos")
@RequiredArgsConstructor
public class BultoController {

    private final BultoService bultoService;

    private JwtAuthFilter.JwtUserDetails auth() {
        return (JwtAuthFilter.JwtUserDetails) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getDetails();
    }

    // =========================================================
    // LISTAR BULTOS DE UN PEDIDO
    // GET /api/bultos?pedidoId=...
    // =========================================================
    @GetMapping
    public ResponseEntity<List<BultoListResponse>> listar(
            @RequestParam String pedidoId) {

        return ResponseEntity.ok(
                bultoService.listar(
                        pedidoId,
                        auth().empresaId()
                )
        );
    }

    // =========================================================
    // PRODUCTOS DEL PEDIDO PARA PACKING
    // GET /api/bultos/pedido/{pedidoId}/productos
    // =========================================================
    @GetMapping("/pedido/{pedidoId}/productos")
    public ResponseEntity<List<PedidoPackingProductoDTO>> obtenerProductosDelPedido(
            @PathVariable String pedidoId) {

        return ResponseEntity.ok(
                bultoService.obtenerProductosDelPedido(
                        pedidoId,
                        auth().empresaId()
                )
        );
    }

    // =========================================================
    // OBTENER DETALLE DE UN BULTO
    // GET /api/bultos/{id}
    // =========================================================
    @GetMapping("/{id}")
    public ResponseEntity<BultoDetailResponse> obtener(
            @PathVariable String id) {

        return ResponseEntity.ok(
                bultoService.obtener(
                        id,
                        auth().empresaId()
                )
        );
    }

    // =========================================================
    // CREAR BULTO
    // POST /api/bultos
    // =========================================================
    @PostMapping
    public ResponseEntity<BultoDetailResponse> crear(
            @Valid @RequestBody CrearBultoRequest request) {

        BultoDetailResponse creado = bultoService.crear(
                request,
                auth().userId(),
                auth().empresaId()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(creado);
    }

    // =========================================================
    // AGREGAR PRODUCTO AL BULTO
    // POST /api/bultos/{id}/productos
    // =========================================================
    @PostMapping("/{id}/productos")
    public ResponseEntity<BultoProductoDTO> agregarProducto(
            @PathVariable String id,
            @Valid @RequestBody AgregarProductoRequest request) {

        return ResponseEntity.ok(
                bultoService.agregarProducto(
                        id,
                        request,
                        auth().empresaId()
                )
        );
    }

    // =========================================================
    // QUITAR PRODUCTO DEL BULTO
    // DELETE /api/bultos/{id}/productos/{detalleId}
    // =========================================================
    @DeleteMapping("/{id}/productos/{detalleId}")
    public ResponseEntity<Void> quitarProducto(
            @PathVariable String id,
            @PathVariable String detalleId) {

        bultoService.quitarProducto(
                id,
                detalleId,
                auth().empresaId()
        );

        return ResponseEntity.noContent().build();
    }

    // =========================================================
    // REGISTRAR PESO
    // PUT /api/bultos/{id}/peso
    // =========================================================
    @PutMapping("/{id}/peso")
    public ResponseEntity<BultoDetailResponse> registrarPeso(
            @PathVariable String id,
            @Valid @RequestBody RegistrarPesoRequest request) {

        return ResponseEntity.ok(
                bultoService.registrarPeso(
                        id,
                        request,
                        auth().empresaId()
                )
        );
    }

    // =========================================================
    // CERRAR BULTO
    // POST /api/bultos/{id}/cerrar
    // =========================================================
    @PostMapping("/{id}/cerrar")
    public ResponseEntity<BultoDetailResponse> cerrar(
            @PathVariable String id) {

        return ResponseEntity.ok(
                bultoService.cerrar(
                        id,
                        auth().empresaId()
                )
        );
    }

    // =========================================================
    // ELIMINAR BULTO
    // DELETE /api/bultos/{id}
    // =========================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String id) {

        bultoService.eliminar(
                id,
                auth().empresaId()
        );

        return ResponseEntity.noContent().build();
    }
}