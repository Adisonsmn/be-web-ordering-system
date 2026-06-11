package com.aromasenja.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO untuk mengubah kata sandi. */
public record ChangePasswordRequest(
        @NotBlank(message = "Password lama tidak boleh kosong")
        String oldPassword,

        @NotBlank(message = "Password baru tidak boleh kosong")
        @Size(min = 8, message = "Password baru minimal 8 karakter")
        String newPassword
) {}
