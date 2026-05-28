package com.aromasenja.domain.pesanan.dto;

import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record StrukPesananResponse(
    UUID pesananId,
    String kodePesanan,
    LocalDateTime tanggalPesanan,
    Integer nomorMeja,
    MetodePembayaran metodePembayaran,
    BigDecimal subtotal,
    BigDecimal diskonPoin,
    BigDecimal diskonPromo,
    BigDecimal totalAkhir,
    List<StrukItem> items
) {
    public record StrukItem(
        String menuName,
        Integer quantity,
        BigDecimal hargaSetelahDiskon,
        BigDecimal subTotal
    ) {}
}
