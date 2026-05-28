package com.aromasenja.domain.laporan;

import com.aromasenja.domain.laporan.dto.*;
import java.util.List;

public interface LaporanService {

    DashboardStatsResponse getDashboardStats();

    List<PendapatanTrendResponse> getPendapatanTrend(String period, Integer bulan, Integer tahun);

    List<MenuTerlarisResponse> getMenuTerlaris(String period, String category, Integer limit);

    List<RatingSentimenResponse> getRatingSentimen();

    PoinPromoStatsResponse getPoinPromoStats();

    byte[] exportLaporan(String period, String format);
}
