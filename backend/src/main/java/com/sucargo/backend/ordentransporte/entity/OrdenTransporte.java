package com.sucargo.backend.ordentransporte.entity;

import com.sucargo.backend.almacen.entity.Almacen;
import com.sucargo.backend.cliente.entity.PuntoEntrega;
import com.sucargo.backend.distribucion.entity.Distribucion;
import com.sucargo.backend.pedido.entity.Pedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "orden_transporte")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenTransporte {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "numero", length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "distribucion_id", nullable = false)
    private Distribucion distribucion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_almacen_id")
    private Almacen origenAlmacen;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destino_punto_entrega_id", nullable = false)
    private PuntoEntrega destinoPuntoEntrega;

    @Column(name = "total_bultos", nullable = false)
    private Integer totalBultos;

    @Column(name = "peso_total", precision = 10, scale = 3)
    private BigDecimal pesoTotal;

    @Column(name = "ventana_inicio")
    private LocalTime ventanaInicio;

    @Column(name = "ventana_fin")
    private LocalTime ventanaFin;

    @Column(
        name = "estado",
        nullable = false,
        columnDefinition = "ENUM('EMITIDA','ANULADA')"
    )
    private String estado;
}