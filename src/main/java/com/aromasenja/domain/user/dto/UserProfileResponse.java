package com.aromasenja.domain.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO response untuk data profil pengguna.
 * Digunakan oleh UserController dan juga sebagai nested response di LoginResponse.
 *
 * Catatan:
 * - statusMember dan totalPoint null untuk ADMIN (bukan client)
 * - password TIDAK pernah ada di response ini
 */
public record UserProfileResponse(
        UUID userId,
        String email,
        String name,
        String phone,
        String role,          // "ADMIN" atau "CLIENT"
        String statusMember,  // "REGULAR", "PREMIUM" — null jika admin
        Integer totalPoint,   // null jika admin
        String avatarUrl,
        LocalDateTime createdAt
) {}
