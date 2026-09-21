package com.sucargo.backend.transportista.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transportista")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transportista {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @Column(name = "empresa_id", nullable = false, columnDefinition = "CHAR(36)")
    private String empresaId;

    @Column(name = "razon_social", nullable = false, length = 150)
    private String razonSocial;

    @Column(length = 11)
    private String ruc;

    @Column(length = 150)
    private String contacto;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Column(name = "tipo_servicio", length = 50)
    private String tipoServicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Estado estado = Estado.ACTIVO;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public enum Estado {
        ACTIVO,
        INACTIVO
    }
}