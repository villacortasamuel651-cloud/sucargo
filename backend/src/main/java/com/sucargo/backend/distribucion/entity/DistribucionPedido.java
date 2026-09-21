package com.sucargo.backend.distribucion.entity;

import com.sucargo.backend.pedido.entity.Pedido;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "distribucion_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistribucionPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "distribucion_id", nullable = false)
    private Distribucion distribucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(nullable = false)
    private Integer secuencia;
}