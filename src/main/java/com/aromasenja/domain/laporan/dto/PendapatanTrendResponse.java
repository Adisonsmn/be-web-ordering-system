package com.aromasenja.domain.laporan.dto;

import java.math.BigDecimal;

public record PendapatanTrendResponse(
    String label,
    BigDecimal totalPendapatan,
    long totalPesanan
) {}
