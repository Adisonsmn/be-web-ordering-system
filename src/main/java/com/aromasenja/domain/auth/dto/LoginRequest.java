package com.aromasenja.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request DTO untuk login dengan email dan password. */
public record LoginRequest(

        @NotBlank(message = "Email tidak boleh kosong")
        @Email(message = "Format email tidak valid")
        String email,

        @NotBlank(message = "Password tidak boleh kosong")
        String password
) {}
