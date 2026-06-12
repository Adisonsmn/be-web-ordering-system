package com.aromasenja.domain.laporan;

import com.aromasenja.common.response.ApiResponse;
import com.aromasenja.domain.laporan.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/laporan")
@RequiredArgsConstructor
@Tag(name = "Analitik & Laporan", description = "Endpoint administratif untuk laporan dashboard, tren keuangan, dan ekspor Excel")
public class LaporanController {

    private final LaporanService laporanService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil metrik KPI harian ringkas + live orders kanban (Admin Only)")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboard() {
        DashboardStatsResponse response = laporanService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil data dashboard", response));
    }

    @GetMapping("/dashboard/delta")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Perbandingan metrik KPI vs hari sebelumnya (Admin Only)")
    public ResponseEntity<ApiResponse<DashboardDeltaResponse>> getDashboardDelta(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate tanggal) {
        DashboardDeltaResponse response = laporanService.getDashboardDelta(tanggal);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil data delta", response));
    }

    @GetMapping("/pendapatan")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil tren data pendapatan per periode bulanan / tahunan (Admin Only)")
    public ResponseEntity<ApiResponse<List<PendapatanTrendResponse>>> getPendapatanTrend(
            @RequestParam(defaultValue = "bulanan") String period,
            @RequestParam(required = false) Integer bulan,
            @RequestParam(required = false) Integer tahun) {
        List<PendapatanTrendResponse> response = laporanService.getPendapatanTrend(period, bulan, tahun);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil data tren pendapatan", response));
    }

    @GetMapping("/menu-terlaris")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil ranking menu terlaris berdasarkan volume penjualan (Admin Only)")
    public ResponseEntity<ApiResponse<List<MenuTerlarisResponse>>> getMenuTerlaris(
            @RequestParam(defaultValue = "bulanan") String period,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "10") Integer limit) {
        List<MenuTerlarisResponse> response = laporanService.getMenuTerlaris(period, category, limit);
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil data menu terlaris", response));
    }

    @GetMapping("/rating-sentimen")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil distribusi jumlah ulasan per bintang (Admin Only)")
    public ResponseEntity<ApiResponse<List<RatingSentimenResponse>>> getRatingSentimen() {
        List<RatingSentimenResponse> response = laporanService.getRatingSentimen();
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil data distribusi rating", response));
    }

    @GetMapping("/poin-promo")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ambil metrik ringkasan penggunaan poin & efektivitas diskon promo (Admin Only)")
    public ResponseEntity<ApiResponse<PoinPromoStatsResponse>> getPoinPromoStats() {
        PoinPromoStatsResponse response = laporanService.getPoinPromoStats();
        return ResponseEntity.ok(ApiResponse.success("Berhasil mengambil data statistik poin & promo", response));
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Download file excel laporan (Admin Only)")
    public ResponseEntity<Resource> exportLaporan(
            @RequestParam(defaultValue = "bulanan") String period,
            @RequestParam(defaultValue = "xlsx") String format) {
        byte[] bytes = laporanService.exportLaporan(period, format);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        String filename = "laporan-aroma-senja-" + period + ".xlsx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .contentLength(bytes.length)
                .body(resource);
    }
}
