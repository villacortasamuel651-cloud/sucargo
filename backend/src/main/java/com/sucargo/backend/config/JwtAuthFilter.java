package com.sucargo.backend.config;

import com.sucargo.backend.shared.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", dejamos pasar sin autenticar
        // (SecurityConfig decide si esa ruta requiere auth o no)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // quita "Bearer "

        if (jwtUtil.isTokenValid(token)) {
            String userId = jwtUtil.extractUserId(token);
            String empresaId = jwtUtil.extractEmpresaId(token);
            String rol = jwtUtil.extractRol(token);

            // Creamos un "usuario autenticado" para Spring Security, usando
            // el userId como principal y el rol como authority (ROLE_XXX)
            var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol));

            var authToken = new UsernamePasswordAuthenticationToken(
                    userId, null, authorities
            );

            // Guardamos empresaId y rol como "details" para poder leerlos
            // después en el service (ej. AuthService, o cualquier otro módulo)
            authToken.setDetails(new JwtUserDetails(userId, empresaId, rol));

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    // Record simple para llevar empresaId y rol junto con la autenticación
    public record JwtUserDetails(String userId, String empresaId, String rol) {}
}