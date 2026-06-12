package com.aromasenja.domain.poin.dto;

import java.time.LocalDateTime;

public record PoinBalanceResponse(
    Integer totalPoint,
    Integer rupiahPerPoin,
    String namaClient,
    LocalDateTime memberSejak,
    Integer totalPoinDiperoleh   // total poin EARN all-time milik client ini
) {}
