package com.aromasenja.domain.keranjang.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record KeranjangResponse(
    UUID keranjangId,
    UUID clientId,
    UUID sessionId,
    List<DetailKeranjangResponse> items,
    BigDecimal totalHarga
) {}
