package com.aromasenja.domain.laporan.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record PoinPromoStatsResponse(
    long totalPoinDiterbitkan,      // total poin EARN (diterbitkan ke pelanggan)
    long totalPoinDigunakan,        // total poin REDEEM (digunakan pelanggan)
    BigDecimal totalDiskonPromo,    // total rupiah diskon dari promo
    long totalPesananPakaiPromo,    // jumlah pesanan yang pakai promo
    TopPromoResponse topPromo
) {
    public record TopPromoResponse(
        UUID promoId,
        String namaPromo,
        long totalPenggunaan
    ) {}
}

