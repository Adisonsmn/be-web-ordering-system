package com.aromasenja.domain.laporan.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PoinPromoStatsResponse(
    long totalPoinRedeemed,
    BigDecimal totalNilaiRedeemRupiah,
    BigDecimal totalDiskonPromo,
    long totalPesananPakaiPromo,
    TopPromoResponse topPromo
) {
    public record TopPromoResponse(
        UUID promoId,
        String namaPromo,
        long totalPenggunaan
    ) {}
}
