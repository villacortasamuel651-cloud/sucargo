package com.sucargo.backend.dashboard.service;

import com.sucargo.backend.dashboard.dto.AlertaDTO;
import com.sucargo.backend.dashboard.dto.PedidoRecienteDTO;
import com.sucargo.backend.dashboard.dto.ResumenDashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public ResumenDashboardResponse obtenerResumen(String empresaId) {
        int pedidos = contarPedidos(empresaId);
        int picking = contarPedidosEnPicking(empresaId);
        int listosDespacho = contarPedidosListosDespacho(empresaId);
        int productosStockBajo = contarProductosStockBajo(empresaId);
        int pedidosSinStock = contarPedidosSinStock(empresaId);

        List<PedidoRecienteDTO> pedidosRecientes = obtenerPedidosRecientes(empresaId);
        List<AlertaDTO> alertas = construirAlertas(productosStockBajo, pedidosSinStock);

        return new ResumenDashboardResponse(
                pedidos, picking, listosDespacho, productosStockBajo,
                pedidosRecientes, alertas
        );
    }

    private int contarPedidos(String empresaId) {
        String sql = """
                SELECT COUNT(*) FROM pedido
                WHERE empresa_id = ? AND estado <> 'CANCELADO'
                """;
        Integer resultado = jdbcTemplate.queryForObject(sql, Integer.class, empresaId);
        return resultado != null ? resultado : 0;
    }

    private int contarPedidosEnPicking(String empresaId) {
        String sql = """
                SELECT COUNT(*) FROM pedido
                WHERE empresa_id = ?
                AND estado IN ('EN_PREPARACION', 'PICKING_COMPLETADO')
                """;
        Integer resultado = jdbcTemplate.queryForObject(sql, Integer.class, empresaId);
        return resultado != null ? resultado : 0;
    }

    private int contarPedidosListosDespacho(String empresaId) {
        String sql = """
                SELECT COUNT(*) FROM pedido
                WHERE empresa_id = ? AND estado = 'LISTO_PARA_DESPACHO'
                """;
        Integer resultado = jdbcTemplate.queryForObject(sql, Integer.class, empresaId);
        return resultado != null ? resultado : 0;
    }

    private int contarPedidosSinStock(String empresaId) {
        String sql = """
                SELECT COUNT(*) FROM pedido
                WHERE empresa_id = ? AND estado = 'SIN_STOCK'
                """;
        Integer resultado = jdbcTemplate.queryForObject(sql, Integer.class, empresaId);
        return resultado != null ? resultado : 0;
    }

    private int contarProductosStockBajo(String empresaId) {
        String sql = """
                SELECT COUNT(*) FROM (
                    SELECT p.id
                    FROM producto p
                    JOIN inventario i ON i.producto_id = p.id
                    WHERE p.empresa_id = ?
                    GROUP BY p.id, p.stock_minimo
                    HAVING SUM(i.stock_fisico) < p.stock_minimo
                ) AS productos_bajo_stock
                """;
        Integer resultado = jdbcTemplate.queryForObject(sql, Integer.class, empresaId);
        return resultado != null ? resultado : 0;
    }

    private List<PedidoRecienteDTO> obtenerPedidosRecientes(String empresaId) {
        String sql = """
                SELECT ped.id, ped.codigo, ped.created_at, ped.estado, ped.prioridad, c.razon_social
                FROM pedido ped
                JOIN cliente c ON c.id = ped.cliente_id
                WHERE ped.empresa_id = ?
                ORDER BY ped.created_at DESC
                LIMIT 5
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PedidoRecienteDTO(
                rs.getString("id"),
                rs.getString("codigo"),
                rs.getString("razon_social"),
                rs.getTimestamp("created_at").toInstant().toString(),
                rs.getString("estado"),
                rs.getString("prioridad")
        ), empresaId);
    }

    private List<AlertaDTO> construirAlertas(int productosStockBajo, int pedidosSinStock) {
        List<AlertaDTO> alertas = new ArrayList<>();

        if (productosStockBajo > 0) {
            alertas.add(new AlertaDTO(
                    "STOCK_BAJO",
                    productosStockBajo + " productos con stock bajo"
            ));
        }

        if (pedidosSinStock > 0) {
            alertas.add(new AlertaDTO(
                    "PEDIDO_SIN_STOCK",
                    pedidosSinStock + " pedidos con falta de stock"
            ));
        }

        // TODO: alerta PEDIDO_PENDIENTE (pedidos en CREADO/CONFIRMADO hace
        // más de X horas sin avanzar) — el contrato deja el umbral "a
        // definir", así que queda pendiente para cuando se afine esa regla.

        return alertas;
    }
}