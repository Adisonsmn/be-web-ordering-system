package com.aromasenja.domain.promo.dto;

import com.aromasenja.domain.promo.entity.TipeDiskon;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PromoResponse(
    UUID promoId,
    String namaPromo,
    TipeDiskon tipeDiskon,
    BigDecimal nilaiDiskon,
    LocalDate tanggalMulai,
    LocalDate tanggalSelesai,
    String targetCategory,
    boolean isActive,
    String imageUrl,
    String tag,
    String description
) {}
