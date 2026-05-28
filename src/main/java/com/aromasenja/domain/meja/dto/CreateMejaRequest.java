package com.aromasenja.domain.meja.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateMejaRequest(
    @NotNull(message = "Nomor meja tidak boleh kosong")
    @Positive(message = "Nomor meja harus bilangan positif")
    Integer nomorMeja,

    @NotNull(message = "Zona tidak boleh kosong")
    String zone  // "INDOOR" atau "OUTDOOR"
) {}
