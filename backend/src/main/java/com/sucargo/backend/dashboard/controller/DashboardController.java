package com.sucargo.backend.dashboard.controller;

import com.sucargo.backend.config.JwtAuthFilter;
import com.sucargo.backend.dashboard.dto.ResumenDashboardResponse;
import com.sucargo.backend.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/resumen")
    public ResponseEntity<ResumenDashboardResponse> obtenerResumen() {
        String empresaId = extraerEmpresaIdDelToken();
        return ResponseEntity.ok(dashboardService.obtenerResumen(empresaId));
    }

    private String extraerEmpresaIdDelToken() {
        var auth = (UsernamePasswordAuthenticationToken)
                SecurityContextHolder.getContext().getAuthentication();
        var details = (JwtAuthFilter.JwtUserDetails) auth.getDetails();
        return details.empresaId();
    }
}