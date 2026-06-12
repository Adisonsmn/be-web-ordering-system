package com.aromasenja.domain.auth.dto;

import com.aromasenja.domain.user.dto.UserProfileResponse;

/**
 * Response DTO untuk login, registrasi, dan refresh token.
 *
 * - accessToken : JWT yang dikirim di header "Authorization: Bearer {token}"
 * - refreshToken: JWT untuk mendapatkan access token baru (simpan di secure storage)
 * - tokenType   : selalu "Bearer"
 * - expiresIn   : durasi access token dalam detik (bukan millisecond)
 * - user        : data profil — null untuk guest (tidak ada profil DB)
 */
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserProfileResponse user
) {}
