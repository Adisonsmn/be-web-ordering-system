package com.aromasenja.domain.meja.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateMejaStatusRequest(
    @NotNull(message = "Status okupansi harus diisi")
    Boolean isOccupied
) {}
