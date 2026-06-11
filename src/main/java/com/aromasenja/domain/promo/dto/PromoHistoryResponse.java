package com.aromasenja.domain.promo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PromoHistoryResponse(
    UUID pesananId,
    String kodePesanan,
    String clientName,
    Integer nomorMeja,
    LocalDateTime tanggalPesanan,
    BigDecimal totalHarga,
    BigDecimal totalPotongan
) {}
