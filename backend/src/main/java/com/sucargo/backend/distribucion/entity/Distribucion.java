package com.sucargo.backend.distribucion.entity;

import com.sucargo.backend.transportista.entity.Transportista;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "distribucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Distribucion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "CHAR(36)")
    private String empresaId;

    @Column(nullable = false, length = 30)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transportista_id", nullable = false)
    private Transportista transportista;

    @Column(nullable = false)
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.ABIERTA;

    @OneToMany(mappedBy = "distribucion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DistribucionPedido> pedidos = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public enum Estado {
        ABIERTA,
        CONFIRMADA,
        CANCELADA
    }
}