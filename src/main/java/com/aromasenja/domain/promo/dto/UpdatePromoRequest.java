package com.aromasenja.domain.promo.dto;

import com.aromasenja.domain.promo.entity.TipeDiskon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdatePromoRequest(
    @NotBlank(message = "Nama promo tidak boleh kosong")
    String namaPromo,

    @NotNull(message = "Tipe diskon wajib dipilih")
    TipeDiskon tipeDiskon,

    @NotNull(message = "Nilai diskon wajib diisi")
    @Positive(message = "Nilai diskon harus positif")
    BigDecimal nilaiDiskon,

    @NotNull(message = "Tanggal mulai wajib diisi")
    LocalDate tanggalMulai,

    @NotNull(message = "Tanggal selesai wajib diisi")
    LocalDate tanggalSelesai,

    String targetCategory,
    String imageUrl,
    String tag,
    String description
) {}
