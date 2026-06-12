package com.aromasenja.domain.meja.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "meja")
@Getter
@Setter
@NoArgsConstructor
public class Meja {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "meja_id")
    private UUID mejaId;

    @Column(name = "nomor_meja", nullable = false, unique = true)
    private Integer nomorMeja;

    @Column(name = "qr_code_url")
    private String qrCodeUrl;

    @Column(name = "zone", nullable = false)
    private ZoneMeja zone = ZoneMeja.INDOOR;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_occupied", nullable = false)
    private boolean isOccupied = false;

    @Column(name = "created_by")
    private UUID createdBy;

    @Version
    private Long version;
}
