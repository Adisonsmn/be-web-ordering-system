package com.aromasenja.domain.config_resto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UpdateRestoConfigRequest(
    @NotNull(message = "Status buka/tutup wajib diisi")
    @JsonProperty("isOpen")
    Boolean isOpen,

    @NotNull(message = "Jam buka wajib diisi")
    LocalTime openTime,

    @NotNull(message = "Jam tutup wajib diisi")
    LocalTime closeTime,

    @NotBlank(message = "Nama restoran tidak boleh kosong")
    String nama,

    @NotBlank(message = "Tagline tidak boleh kosong")
    String tagline,

    String alamat,
    String telepon,
    String email,
    String instagram
) {}
