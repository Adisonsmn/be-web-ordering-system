package com.aromasenja.domain.config_resto.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;

public record RestoConfigResponse(
    @JsonProperty("isOpen")
    boolean isOpen,
    @JsonFormat(pattern = "HH:mm")
    LocalTime openTime,
    @JsonFormat(pattern = "HH:mm")
    LocalTime closeTime,
    String namaRestoran,
    String tagline,
    String alamat,
    String telepon,
    String email,
    String instagram
) {}
