package com.aromasenja.domain.poin.dto;

import java.math.BigDecimal;

public record PoinKalkulasiResponse(
    BigDecimal diskonRupiah,
    BigDecimal totalSetelahDiskon
) {}
