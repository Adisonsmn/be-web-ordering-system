package com.aromasenja.domain.rating.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateRatingRequest(
    @NotNull(message = "Pesanan ID wajib diisi")
    UUID pesananId,

    @NotNull(message = "Rating overall wajib diisi")
    @Min(value = 1, message = "Bintang minimal 1")
    @Max(value = 5, message = "Bintang maksimal 5")
    Integer ratingOverall,

    String ulasanOverall,

    Boolean isPublic,

    @Valid
    List<ItemRatingRequest> items
) {
    public record ItemRatingRequest(
        @NotNull(message = "Menu ID wajib diisi")
        UUID menuId,

        @NotNull(message = "Bintang wajib diisi")
        @Min(value = 1, message = "Bintang minimal 1")
        @Max(value = 5, message = "Bintang maksimal 5")
        Short bintang,

        String ulasan
    ) {}
}
