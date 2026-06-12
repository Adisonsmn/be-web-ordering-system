package com.aromasenja.domain.keranjang.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DetailKeranjangResponse(
    UUID detailKeranjangId,
    UUID menuId,
    String menuName,
    BigDecimal price,
    String imageUrl,
    Integer quantity,
    String catatan,
    BigDecimal subtotal
) {}
