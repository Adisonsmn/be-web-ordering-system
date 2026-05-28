package com.aromasenja.domain.config_resto.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRequest(
    @NotBlank(message = "Nama tidak boleh kosong")
    String name,

    String phone,

    @NotNull(message = "Status aktif wajib diisi")
    Boolean isActive
) {}
