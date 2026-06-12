package com.aromasenja.domain.menu.dto;

import java.util.UUID;

/**
 * Request untuk PATCH /api/menu/{menuId}/promo
 * Mengatur promo pada satu menu tertentu.
 * promoId = null berarti hapus promo dari menu.
 */
public record UpdateMenuPromoRequest(
    UUID promoId
) {}
