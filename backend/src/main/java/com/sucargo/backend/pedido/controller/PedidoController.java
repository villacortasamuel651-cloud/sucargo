package com.sucargo.backend.pedido.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.pedido.dto.*;
import com.sucargo.backend.pedido.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.sucargo.backend.packing.dto.CompletarPackingResponse;
import com.sucargo.backend.packing.service.BultoService;
import java.util.List;
import java.util.Set;
import com.sucargo.backend.almacen.dto.AlmacenResumenDTO;;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private static final Set<String> ROLES_CREAR_CANCELAR = Set.of("ADMIN_EMPRESA", "VENTAS");

    private final PedidoService pedidoService;
    private final BultoService bultoService;

    @GetMapping
    public ResponseEntity<List<PedidoListResponse>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String clienteId,
            @RequestParam(required = false) String q) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(pedidoService.listar(empresaId, estado, clienteId, q));
    }

    @GetMapping("/disponibles-distribucion")
    public ResponseEntity<List<PedidoListResponse>> listarDisponiblesParaDistribucion() {
        String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity.ok(
            pedidoService.listarDisponiblesParaDistribucion(empresaId));
    }

    @PostMapping("/almacenes-disponibles")
    public ResponseEntity<List<AlmacenResumenDTO>> obtenerAlmacenesDisponibles(
        @Valid @RequestBody AlmacenesDisponiblesRequest request) {

    String empresaId = extraerEmpresaIdDelToken();

    return ResponseEntity.ok(
            pedidoService.obtenerAlmacenesDisponibles(
                    request,
                    empresaId
            )
    );
    }





    @GetMapping("/{id}")
    public ResponseEntity<PedidoDetailResponse> obtener(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(pedidoService.obtener(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<PedidoDetailResponse> crear(@Valid @RequestBody PedidoRequest request) {
        validarRolPermitido();
        String empresaId = extraerEmpresaIdDelToken();
        String usuarioId = extraerUserIdDelToken();
        PedidoDetailResponse creado = pedidoService.crear(request, usuarioId, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoDetailResponse> cancelar(@PathVariable String id) {
        validarRolPermitido();
        String empresaId = extraerEmpresaIdDelToken();
        String usuarioId = extraerUserIdDelToken();
        return ResponseEntity.ok(pedidoService.cancelar(id, usuarioId, empresaId));
    }

    private void validarRolPermitido() {
        String rol = extraerRolDelToken();
        if (!ROLES_CREAR_CANCELAR.contains(rol)) {
            throw new com.sucargo.backend.pedido.service.PedidoInvalidoException(
                    "Tu rol no está autorizado para esta acción");
        }
    }
    @PostMapping("/{id}/completar-packing")
    public ResponseEntity<CompletarPackingResponse> completarPacking(
        @PathVariable String id) {

    String empresaId = extraerEmpresaIdDelToken();

        return ResponseEntity.ok(
            bultoService.completarPacking(id, empresaId)
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