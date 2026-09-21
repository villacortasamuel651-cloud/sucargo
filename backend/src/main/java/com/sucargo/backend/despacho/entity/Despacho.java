package com.sucargo.backend.despacho.entity;

import com.sucargo.backend.ordentransporte.entity.OrdenTransporte;
import com.sucargo.backend.pedido.entity.Pedido;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "despacho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_transporte_id")
    private OrdenTransporte ordenTransporte;

    @Column(name = "usuario_id", columnDefinition = "CHAR(36)")
    private String usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @Column(name = "fecha_despacho")
    private LocalDateTime fechaDespacho;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "despacho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DespachoBultoVerificado> bultos;

    public enum Estado {
        EN_VERIFICACION,
        DESPACHADO
    }
}