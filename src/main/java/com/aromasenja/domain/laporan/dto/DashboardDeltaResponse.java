package com.aromasenja.domain.laporan.dto;

public record DashboardDeltaResponse(
    MetrikDelta pendapatan,
    MetrikDelta totalPesanan,
    MetrikDelta mejaAktif,
    MetrikDelta ratingRata
) {
    public record MetrikDelta(
        Double nilaiHariIni,
        Double nilaiKemarin,
        Double deltaPersen,
        String deltaArah
    ) {}
}
