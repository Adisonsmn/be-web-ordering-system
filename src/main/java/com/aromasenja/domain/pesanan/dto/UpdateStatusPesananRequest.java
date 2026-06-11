package com.aromasenja.domain.pesanan.dto;

import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record UpdateStatusPesananRequest(
    @NotNull(message = "Status wajib ditentukan")
    StatusPesanan status,

    @Min(value = 1, message = "Estimasi minimal 1 menit")
    @Max(value = 180, message = "Estimasi maksimal 180 menit")
    Integer estimasiMenit
) {}
