package com.sucargo.backend.picking.entity;

import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.pedido.entity.Pedido;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "picking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Picking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.PENDIENTE;

    @Column(name = "fecha_inicio")
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @OneToMany(mappedBy = "picking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PickingDetalle> lineas = new ArrayList<>();

    public enum Estado {
        PENDIENTE,
        EN_PROCESO,
        COMPLETADO,
        COMPLETADO_CON_INCIDENCIAS
    }
}