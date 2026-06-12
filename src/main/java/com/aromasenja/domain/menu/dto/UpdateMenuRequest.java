package com.aromasenja.domain.menu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UpdateMenuRequest(
    @NotBlank(message = "Nama menu tidak boleh kosong")
    String menuName,

    @NotNull(message = "Harga tidak boleh kosong")
    @PositiveOrZero(message = "Harga tidak boleh negatif")
    BigDecimal price,

    String description,

    @NotBlank(message = "Kategori tidak boleh kosong")
    String category,

    String imageUrl,
    UUID promoId,
    String titleLine1,
    String titleLine2,
    String longDescription,
    String heroImageUrl,
    Boolean showDoneness,
    List<String> donenessOptions,
    List<String> spiceOptions
) {}
