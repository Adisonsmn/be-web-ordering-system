package com.aromasenja.domain.menu.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateMenuAvailabilityRequest(
    @NotNull(message = "Status ketersediaan tidak boleh kosong")
    Boolean isAvailable
) {}
