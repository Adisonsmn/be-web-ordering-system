package com.aromasenja.domain.keranjang.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AddKeranjangItemRequest(
    @NotNull(message = "menuId tidak boleh kosong")
    UUID menuId,

    @NotNull(message = "quantity tidak boleh kosong")
    @Min(value = 1, message = "quantity minimal 1")
    Integer quantity,

    String catatan
) {}
