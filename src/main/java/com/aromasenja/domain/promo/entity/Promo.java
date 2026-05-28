package com.aromasenja.domain.promo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "promo")
@Getter @Setter @NoArgsConstructor
public class Promo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "promo_id")
    private UUID promoId;

    @Column(name = "nama_promo", nullable = false)
    private String namaPromo;

    @Column(name = "tipe_diskon", nullable = false)
    private TipeDiskon tipeDiskon;

    @Column(name = "nilai_diskon", nullable = false, precision = 15, scale = 2)
    private BigDecimal nilaiDiskon;

    @Column(name = "tanggal_mulai", nullable = false)
    private LocalDate tanggalMulai;

    @Column(name = "tanggal_selesai", nullable = false)
    private LocalDate tanggalSelesai;

    @Column(name = "target_category")
    private String targetCategory;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "tag")
    private String tag;

    @Column(name = "description")
    private String description;
}
