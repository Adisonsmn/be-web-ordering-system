package com.aromasenja.domain.poin.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PoinRiwayatResponse(
    UUID poinTransaksiId,
    UUID pesananId,
    String kodePesanan,
    Integer jumlahPoin,
    String tipe, // "earn" / "redeem"
    LocalDateTime createdAt
) {}
