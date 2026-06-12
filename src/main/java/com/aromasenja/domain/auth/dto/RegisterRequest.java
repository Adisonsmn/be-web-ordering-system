package com.aromasenja.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Request DTO untuk registrasi member baru. */
public record RegisterRequest(

        @NotBlank(message = "Email tidak boleh kosong")
        @Email(message = "Format email tidak valid")
        String email,

        @NotBlank(message = "Password tidak boleh kosong")
        @Size(min = 8, message = "Password minimal 8 karakter")
        String password,

        @NotBlank(message = "Nama tidak boleh kosong")
        @Size(min = 2, max = 100, message = "Nama harus antara 2-100 karakter")
        String name,

        /** Nomor telepon opsional. */
        @Pattern(
                regexp = "^(\\+62|62|0)[0-9]{8,13}$",
                message = "Format nomor telepon tidak valid"
        )
        String phone
) {}
