package com.aromasenja.domain.keranjang.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateKeranjangItemRequest(
    @NotNull(message = "quantity tidak boleh kosong")
    @Min(value = 0, message = "quantity minimal 0")
    Integer quantity,

    String catatan
) {}
