package com.aromasenja.domain.menu.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuResponse(
    UUID menuId,
    String menuName,
    BigDecimal price,
    String description,
    String category,
    boolean isAvailable,
    String imageUrl,
    PromoMinResponse promo
) {
    public record PromoMinResponse(
        UUID promoId,
        String namaPromo,
        String tipeDiskon,
        BigDecimal nilaiDiskon
    ) {}
}
