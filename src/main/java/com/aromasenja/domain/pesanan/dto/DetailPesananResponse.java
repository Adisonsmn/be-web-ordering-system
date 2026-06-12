package com.aromasenja.domain.pesanan.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DetailPesananResponse(
    UUID detailPesananId,
    UUID menuId,
    String menuName,
    String imageUrl,
    Integer quantity,
    String catatan,
    BigDecimal hargaSnapshot,
    BigDecimal hargaSetelahDiskon,
    BigDecimal subTotal
) {}
