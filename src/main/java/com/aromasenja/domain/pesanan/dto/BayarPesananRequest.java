package com.aromasenja.domain.pesanan.dto;

import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record BayarPesananRequest(
    @NotNull(message = "Metode pembayaran wajib ditentukan")
    MetodePembayaran metodePembayaran,

    @NotNull(message = "Jumlah dibayar wajib diisi")
    @Positive(message = "Jumlah dibayar harus lebih dari 0")
    BigDecimal jumlahDibayar
) {}
