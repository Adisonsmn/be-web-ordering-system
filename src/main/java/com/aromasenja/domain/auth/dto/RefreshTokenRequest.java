package com.aromasenja.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/** Request DTO untuk refresh access token menggunakan refresh token. */
public record RefreshTokenRequest(

        @NotBlank(message = "Refresh token tidak boleh kosong")
        String refreshToken
) {}
