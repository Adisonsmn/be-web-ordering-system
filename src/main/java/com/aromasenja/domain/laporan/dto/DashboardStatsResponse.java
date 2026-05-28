package com.aromasenja.domain.laporan.dto;

import com.aromasenja.domain.pesanan.dto.PesananResponse;
import java.math.BigDecimal;
import java.util.List;

public record DashboardStatsResponse(
    BigDecimal pendapatanHariIni,
    long totalPesananHariIni,
    long totalMejaAktif,
    double avgRatingHariIni,
    BigDecimal avgOrderValue,
    long totalPoinRedeemed,
    BigDecimal totalDiskonPromo,
    List<PesananResponse> liveOrders
) {}
