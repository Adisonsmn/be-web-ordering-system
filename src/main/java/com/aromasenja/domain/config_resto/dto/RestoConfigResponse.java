package com.aromasenja.domain.config_resto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;

public record RestoConfigResponse(
    @JsonProperty("isOpen")
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
