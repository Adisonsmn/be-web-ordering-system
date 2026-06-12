package com.aromasenja.domain.meja.dto;

import jakarta.validation.constraints.NotBlank;

public record ScanMejaRequest(
    @NotBlank(message = "Device token wajib diisi")
    String deviceToken
) {}
