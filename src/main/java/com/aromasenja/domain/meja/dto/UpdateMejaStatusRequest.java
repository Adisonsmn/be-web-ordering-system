package com.aromasenja.domain.meja.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Request body untuk update status meja oleh admin.
 * - isOccupied: wajib — true = terisi, false = kosong
 */
public record UpdateMejaStatusRequest(
    @NotNull(message = "Status okupansi harus diisi")
    Boolean isOccupied
) {}
