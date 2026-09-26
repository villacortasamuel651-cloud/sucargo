package com.sucargo.backend.empresa.controller;

import com.sucargo.backend.empresa.entity.Empresa;
import com.sucargo.backend.empresa.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<List<Empresa>> listarTodas() {
        return ResponseEntity.ok(empresaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Empresa> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(empresaService.obtenerPorId(id));
    }

    @GetMapping("/activas")
    public ResponseEntity<List<Empresa>> listarActivas() {
        return ResponseEntity.ok(empresaService.listarActivas());
    }

    @GetMapping("/inactivas")
    public ResponseEntity<List<Empresa>> listarInactivas() {
        return ResponseEntity.ok(empresaService.listarInactivas());
    }
}