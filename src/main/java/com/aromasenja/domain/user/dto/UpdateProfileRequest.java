package com.aromasenja.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO request untuk update profil pengguna.
 * Hanya name, phone, dan avatar_url yang bisa diubah oleh user sendiri.
 * Email dan role hanya bisa diubah oleh admin (endpoint terpisah).
 */
public record UpdateProfileRequest(

        @NotBlank(message = "Nama tidak boleh kosong")
        @Size(min = 2, max = 100, message = "Nama harus antara 2-100 karakter")
        String name,

        /**
         * Nomor telepon opsional — boleh null atau kosong untuk menghapus.
         * Format: +62xxx, 62xxx, atau 08xxx.
         */
        @Pattern(
                regexp = "^(\\+62|62|0)[0-9]{8,13}$",
                message = "Format nomor telepon tidak valid (contoh: 08123456789 atau +628123456789)"
        )
        String phone,

        /**
         * URL avatar pengguna — opsional, boleh null.
         * Harus berformat URL valid (http/https) jika diisi.
         */
        @Pattern(
                regexp = "^(https?://.*)?$",
                message = "Format avatar URL tidak valid, harus diawali http:// atau https://"
        )
        String avatarUrl
) {}
