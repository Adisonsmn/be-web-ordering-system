package com.aromasenja.domain.menu.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuDetailResponse(
    UUID menuId,
    String menuName,
    BigDecimal price,
    String description,
    String category,
    boolean isAvailable,
    String imageUrl,
    UUID createdBy,
    UUID updatedBy,
    PromoDetailResponse promo,
    String titleLine1,
    String titleLine2,
    String longDescription,
    String heroImageUrl,
    Boolean showDoneness,
    List<String> donenessOptions,
    List<String> spiceOptions,
    double averageRating
) {
    public record PromoDetailResponse(
        UUID promoId,
        String namaPromo,
        String tipeDiskon,
        BigDecimal nilaiDiskon,
        String description
    ) {}
}
