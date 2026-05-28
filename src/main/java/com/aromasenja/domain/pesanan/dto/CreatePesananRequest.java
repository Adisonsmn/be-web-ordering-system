package com.aromasenja.domain.pesanan.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreatePesananRequest(
    @NotNull(message = "Meja wajib dipilih")
    UUID mejaId,

    String catatanDapur,

    @NotNull(message = "Penggunaan poin wajib ditentukan")
    Boolean gunakanPoin
) {}
