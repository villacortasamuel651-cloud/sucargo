package com.sucargo.backend.pedido.entity;

import com.sucargo.backend.almacen.entity.Almacen;
import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.cliente.entity.Cliente;
import com.sucargo.backend.cliente.entity.PuntoEntrega;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "CHAR(36)")
    private String empresaId;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "punto_entrega_id", nullable = false)
    private PuntoEntrega puntoEntrega;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id", nullable = false)
    private Almacen almacen;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Estado estado = Estado.CREADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", nullable = false)
    @Builder.Default
    private Prioridad prioridad = Prioridad.MEDIA;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PedidoDetalle> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum Estado {
        CREADO,
        CONFIRMADO,
        STOCK_RESERVADO,
        SIN_STOCK,
        EN_PREPARACION,
        PICKING_COMPLETADO,
        PACKING_COMPLETADO,
        LISTO_PARA_DESPACHO,
        DESPACHADO,
        CANCELADO
    }

    public enum Prioridad {
        BAJA,
        MEDIA,
        ALTA
    }
}