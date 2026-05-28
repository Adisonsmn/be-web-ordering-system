package com.aromasenja.domain.config_resto.dto;

import java.time.LocalTime;

public record RestoConfigResponse(
    boolean isOpen,
    LocalTime openTime,
    LocalTime closeTime,
    String namaRestoran,
    String tagline,
    String alamat,
    String telepon,
    String email,
    String instagram
) {}
