package com.aromasenja.domain.poin.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record PoinKalkulasiRequest(
    @NotNull(message = "Pesanan subtotal wajib diisi")
    @PositiveOrZero(message = "Pesanan subtotal tidak boleh negatif")
    BigDecimal pesananSubtotal,

    @NotNull(message = "Poin digunakan wajib diisi")
    @PositiveOrZero(message = "Poin digunakan tidak boleh negatif")
    Integer poinDigunakan
) {}
