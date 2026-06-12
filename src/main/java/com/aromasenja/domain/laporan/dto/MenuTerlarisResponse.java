package com.aromasenja.domain.laporan.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuTerlarisResponse(
    UUID menuId,
    String menuName,
    long totalTerjual,
    BigDecimal totalPendapatan
) {}
