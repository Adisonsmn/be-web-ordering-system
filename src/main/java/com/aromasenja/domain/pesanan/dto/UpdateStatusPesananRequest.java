package com.aromasenja.domain.pesanan.dto;

import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusPesananRequest(
    @NotNull(message = "Status wajib ditentukan")
    StatusPesanan status,

    Integer estimasiMenit
) {}
