package com.aromasenja.domain.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO untuk login sebagai guest via QR Code meja.
 *
 * Flow:
 * 1. User scan QR meja → QR berisi URL dengan mejaId embedded
 * 2. Frontend ekstrak mejaId dari URL
 * 3. Frontend kirim POST /api/auth/guest dengan body ini
 * 4. Server return access token (tanpa refresh token — guest tidak punya sesi persisten)
 */
public record GuestLoginRequest(

        @NotNull(message = "Table ID tidak boleh kosong")
        UUID tableId,

        @NotNull(message = "Device token tidak boleh kosong")
        String deviceToken
) {}
