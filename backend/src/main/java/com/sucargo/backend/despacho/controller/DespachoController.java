package com.sucargo.backend.despacho.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.despacho.dto.*;
import com.sucargo.backend.despacho.service.DespachoInvalidoException;
import com.sucargo.backend.despacho.service.DespachoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/despachos")
@RequiredArgsConstructor
public class DespachoController {

    private static final Set<String> ROLES_OPERAR = Set.of("LOGISTICA", "DESPACHO");

    private final DespachoService despachoService;

    @GetMapping
    public ResponseEntity<List<DespachoListResponse>> listar(
            @RequestParam(required = false) String estado) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(despachoService.listar(empresaId, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DespachoDetailResponse> obtener(@PathVariable String id) {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(despachoService.obtener(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<DespachoDetailResponse> iniciar(
            @Valid @RequestBody IniciarDespachoRequest request) {
        validarRolOperador();
        String empresaId = extraerEmpresaIdDelToken();
        DespachoDetailResponse creado = despachoService.iniciar(request, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PostMapping("/{id}/verificar-bulto")
    public ResponseEntity<DespachoDetailResponse> verificarBulto(
            @PathVariable String id, @Valid @RequestBody VerificarBultoRequest request) {
        validarRolOperador();
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(despachoService.verificarBulto(id, request, empresaId));
    }

    @PostMapping("/{id}/completar")
    public ResponseEntity<CompletarDespachoResponse> completar(@PathVariable String id) {
        validarRolOperador();
        String empresaId = extraerEmpresaIdDelToken();
        String usuarioId = extraerUserIdDelToken();
        return ResponseEntity.ok(despachoService.completar(id, usuarioId, empresaId));
    }

    private void validarRolOperador() {
        String rol = extraerRolDelToken();
        if (!ROLES_OPERAR.contains(rol)) {
            throw new DespachoInvalidoException("Tu rol no está autorizado para esta acción");
        }
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