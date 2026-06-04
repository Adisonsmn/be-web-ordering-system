package com.aromasenja.domain.pesanan.dto;

import java.util.List;

public record KanbanPesananResponse(
    List<PesananResponse> newOrders,
    List<PesananResponse> preparingOrders,
    List<PesananResponse> readyOrders
) {}
