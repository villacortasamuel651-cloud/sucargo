package com.sucargo.backend.packing.entity;

import com.sucargo.backend.auth.entity.Usuario;
import com.sucargo.backend.pedido.entity.Pedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bulto")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bulto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "numero", nullable = false)
    private Integer numero;

    @Column(name = "peso", precision = 10, scale = 3)
    private BigDecimal peso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Builder.Default
    private Estado estado = Estado.ABIERTO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "bulto", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BultoDetalle> productos = new ArrayList<>();

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
        ABIERTO, CERRADO
    }
}