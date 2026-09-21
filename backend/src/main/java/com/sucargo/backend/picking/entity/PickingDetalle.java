package com.sucargo.backend.picking.entity;

import com.sucargo.backend.pedido.entity.PedidoDetalle;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "picking_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PickingDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "picking_id", nullable = false)
    private Picking picking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_detalle_id", nullable = false)
    private PedidoDetalle pedidoDetalle;

    @Column(name = "cantidad_esperada", nullable = false)
    private Integer cantidadEsperada;

    @Column(name = "cantidad_recolectada", nullable = false)
    @Builder.Default
    private Integer cantidadRecolectada = 0;

    @Column(length = 200)
    private String incidencia;
}