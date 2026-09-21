package com.sucargo.backend.despacho.entity;

import com.sucargo.backend.packing.entity.Bulto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "despacho_bulto_verificado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DespachoBultoVerificado {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "CHAR(36)")
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "despacho_id", nullable = false)
    private Despacho despacho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bulto_id", nullable = false)
    private Bulto bulto;

    @Column(name = "verificado", nullable = false)
    @Builder.Default
    private Boolean verificado = false;

    @Column(name = "fecha_verificacion")
    private LocalDateTime fechaVerificacion;
}