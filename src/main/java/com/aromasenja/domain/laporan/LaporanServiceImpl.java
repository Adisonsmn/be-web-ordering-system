package com.aromasenja.domain.laporan;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.domain.laporan.dto.*;
import com.aromasenja.domain.meja.MejaRepository;
import com.aromasenja.domain.pesanan.PesananMapper;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.pesanan.dto.PesananResponse;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.poin.PoinTransaksiRepository;
import com.aromasenja.domain.rating.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import com.aromasenja.domain.pesanan.DetailPesananRepository;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LaporanServiceImpl implements LaporanService {

    private final PesananRepository pesananRepository;
    private final RatingRepository ratingRepository;
    private final MejaRepository mejaRepository;
    private final PoinTransaksiRepository poinTransaksiRepository;
    private final DetailPesananRepository detailPesananRepository;
    private final PesananMapper pesananMapper;

    @Value("${app.poin.rupiah-per-poin}")
    private Integer rupiahPerPoin;

    private static final ZoneId JAKARTA_ZONE = ZoneId.of("Asia/Jakarta");

    @Override
    public DashboardStatsResponse getDashboardStats() {
        LocalDateTime[] range = getTodayRangeJakarta();
        LocalDateTime start = range[0];
        LocalDateTime end = range[1];

        // 1. Pendapatan hari ini (dari pesanan berstatus SERVED)
        BigDecimal pendapatan = pesananRepository.sumTotalHargaByStatusAndTanggalPesananBetween(StatusPesanan.SERVED, start, end);

        // 2. Total pesanan hari ini
        long totalPesanan = pesananRepository.countByTanggalPesananBetween(start, end);

        // 3. Total meja aktif (occupied)
        long mejaAktif = mejaRepository.countByIsActiveTrueAndIsOccupiedTrue();

        // 4. Avg rating hari ini
        double avgRating = ratingRepository.getAverageRatingByCreatedAtBetween(start, end);

        // 5. Avg order value & diskon/poin stats
        List<Pesanan> todayOrders = pesananRepository.findByTanggalPesananBetween(start, end);
        List<Pesanan> servedToday = todayOrders.stream()
                .filter(p -> p.getStatus() == StatusPesanan.SERVED)
                .toList();

        BigDecimal avgOrderValue = BigDecimal.ZERO;
        if (!servedToday.isEmpty()) {
            BigDecimal sumServed = servedToday.stream()
                    .map(Pesanan::getTotalHarga)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            avgOrderValue = sumServed.divide(BigDecimal.valueOf(servedToday.size()), 2, RoundingMode.HALF_UP);
        }

        long totalPoinRedeemed = todayOrders.stream()
                .mapToLong(Pesanan::getPoinDigunakan)
                .sum();

        BigDecimal totalDiskonPromo = detailPesananRepository.getTotalDiskonPromoBetween(start, end);

        // 6. Live orders (NEW, PREPARING, READY)
        List<Pesanan> livePesanan = pesananRepository.findByStatusIn(List.of(StatusPesanan.NEW, StatusPesanan.PREPARING, StatusPesanan.READY));
        List<PesananResponse> liveOrders = livePesanan.stream()
                .map(pesananMapper::toResponse)
                .sorted(Comparator.comparing(PesananResponse::tanggalPesanan).reversed())
                .toList();

        return new DashboardStatsResponse(
                pendapatan,
                totalPesanan,
                mejaAktif,
                avgRating,
                avgOrderValue,
                totalPoinRedeemed,
                totalDiskonPromo,
                liveOrders
        );
    }

    @Override
    public DashboardDeltaResponse getDashboardDelta(LocalDate tanggal) {
        // Range for today (or requested date)
        LocalDateTime startToday = tanggal.atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endToday = tanggal.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

        // Range for yesterday
        LocalDate kemarin = tanggal.minusDays(1);
        LocalDateTime startKemarin = kemarin.atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime endKemarin = kemarin.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

        // 1. Pendapatan
        BigDecimal pToday = pesananRepository.sumTotalHargaByStatusAndTanggalPesananBetween(StatusPesanan.SERVED, startToday, endToday);
        BigDecimal pKemarin = pesananRepository.sumTotalHargaByStatusAndTanggalPesananBetween(StatusPesanan.SERVED, startKemarin, endKemarin);
        DashboardDeltaResponse.MetrikDelta pendapatanDelta = calculateDelta(
                pToday != null ? pToday.doubleValue() : 0.0,
                pKemarin != null ? pKemarin.doubleValue() : 0.0
        );

        // 2. Total Pesanan
        long tpToday = pesananRepository.countNonCancelledByTanggalPesananBetween(startToday, endToday);
        long tpKemarin = pesananRepository.countNonCancelledByTanggalPesananBetween(startKemarin, endKemarin);
        DashboardDeltaResponse.MetrikDelta totalPesananDelta = calculateDelta(
                (double) tpToday,
                (double) tpKemarin
        );

        // 3. Meja Aktif
        long maToday = pesananRepository.countDistinctMejaByTanggalPesananBetween(startToday, endToday);
        long maKemarin = pesananRepository.countDistinctMejaByTanggalPesananBetween(startKemarin, endKemarin);
        DashboardDeltaResponse.MetrikDelta mejaAktifDelta = calculateDelta(
                (double) maToday,
                (double) maKemarin
        );

        // 4. Rating Rata-rata
        double rrToday = ratingRepository.getAverageRatingByCreatedAtBetween(startToday, endToday);
        double rrKemarin = ratingRepository.getAverageRatingByCreatedAtBetween(startKemarin, endKemarin);
        DashboardDeltaResponse.MetrikDelta ratingRataDelta = calculateDelta(
                rrToday,
                rrKemarin
        );

        return new DashboardDeltaResponse(
                pendapatanDelta,
                totalPesananDelta,
                mejaAktifDelta,
                ratingRataDelta
        );
    }

    private DashboardDeltaResponse.MetrikDelta calculateDelta(Double hariIni, Double kemarin) {
        if (kemarin == null || kemarin == 0.0) {
            return new DashboardDeltaResponse.MetrikDelta(hariIni, kemarin, null, "no_data");
        }
        double deltaPersen = ((hariIni - kemarin) / kemarin) * 100.0;
        // round to 1 decimal place
        deltaPersen = Math.round(deltaPersen * 10.0) / 10.0;
        
        String arah = "sama";
        if (deltaPersen > 0) arah = "naik";
        else if (deltaPersen < 0) arah = "turun";

        return new DashboardDeltaResponse.MetrikDelta(hariIni, kemarin, deltaPersen, arah);
    }

    @Override
    public List<PendapatanTrendResponse> getPendapatanTrend(String period, Integer bulan, Integer tahun) {
        ZonedDateTime nowJakarta = ZonedDateTime.now(JAKARTA_ZONE);
        if (tahun == null) tahun = nowJakarta.getYear();

        List<PendapatanTrendResponse> trends = new ArrayList<>();

        if ("bulanan".equalsIgnoreCase(period)) {
            if (bulan == null) bulan = nowJakarta.getMonthValue();
            LocalDate startMonth = LocalDate.of(tahun, bulan, 1);
            LocalDate endMonth = startMonth.with(TemporalAdjusters.lastDayOfMonth());

            LocalDateTime start = startMonth.atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = endMonth.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

            List<Object[]> results = pesananRepository.findDailyRevenueBetween(start, end);

            int totalDays = endMonth.getDayOfMonth();
            Map<Integer, PendapatanTrendResponse> trendMap = new HashMap<>();
            for (int day = 1; day <= totalDays; day++) {
                LocalDate labelDate = LocalDate.of(tahun, bulan, day);
                trendMap.put(day, new PendapatanTrendResponse(labelDate.toString(), BigDecimal.ZERO, 0));
            }

            for (Object[] row : results) {
                LocalDateTime date = parseDateTrunc(row[0]);
                int day = date.atZone(ZoneId.systemDefault()).withZoneSameInstant(JAKARTA_ZONE).getDayOfMonth();

                BigDecimal totalIncome = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                int totalPesanan = row[2] != null ? ((Number) row[2]).intValue() : 0;

                LocalDate labelDate = LocalDate.of(tahun, bulan, day);
                trendMap.put(day, new PendapatanTrendResponse(labelDate.toString(), totalIncome, totalPesanan));
            }

            for (int day = 1; day <= totalDays; day++) {
                trends.add(trendMap.get(day));
            }

        } else if ("tahunan".equalsIgnoreCase(period)) {
            LocalDate startYear = LocalDate.of(tahun, 1, 1);
            LocalDate endYear = LocalDate.of(tahun, 12, 31);

            LocalDateTime start = startYear.atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            LocalDateTime end = endYear.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();

            List<Object[]> results = pesananRepository.findMonthlyRevenueBetween(start, end);

            String[] monthLabels = {"Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"};

            Map<Integer, PendapatanTrendResponse> trendMap = new HashMap<>();
            for (int m = 1; m <= 12; m++) {
                trendMap.put(m, new PendapatanTrendResponse(monthLabels[m - 1] + " " + tahun, BigDecimal.ZERO, 0));
            }

            for (Object[] row : results) {
                LocalDateTime date = parseDateTrunc(row[0]);
                int month = date.atZone(ZoneId.systemDefault()).withZoneSameInstant(JAKARTA_ZONE).getMonthValue();

                BigDecimal totalIncome = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
                int totalPesanan = row[2] != null ? ((Number) row[2]).intValue() : 0;

                trendMap.put(month, new PendapatanTrendResponse(monthLabels[month - 1] + " " + tahun, totalIncome, totalPesanan));
            }

            for (int m = 1; m <= 12; m++) {
                trends.add(trendMap.get(m));
            }
        }

        return trends;
    }

    private LocalDateTime parseDateTrunc(Object obj) {
        if (obj instanceof LocalDateTime) return (LocalDateTime) obj;
        if (obj instanceof java.sql.Timestamp) return ((java.sql.Timestamp) obj).toLocalDateTime();
        if (obj instanceof java.sql.Date) return ((java.sql.Date) obj).toLocalDate().atStartOfDay();
        if (obj instanceof LocalDate) return ((LocalDate) obj).atStartOfDay();
        String str = obj.toString();
        if (str.length() > 10) {
            return LocalDateTime.parse(str.substring(0, 19).replace(" ", "T"));
        }
        return LocalDate.parse(str.substring(0, 10)).atStartOfDay();
    }

    @Override
    public List<MenuTerlarisResponse> getMenuTerlaris(String period, String category, Integer limit) {
        if (limit == null) limit = 10;

        LocalDateTime start, end;
        ZonedDateTime nowJakarta = ZonedDateTime.now(JAKARTA_ZONE);

        if ("bulanan".equalsIgnoreCase(period)) {
            LocalDate startMonth = LocalDate.of(nowJakarta.getYear(), nowJakarta.getMonthValue(), 1);
            LocalDate endMonth = startMonth.with(TemporalAdjusters.lastDayOfMonth());
            start = startMonth.atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            end = endMonth.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } else if ("tahunan".equalsIgnoreCase(period)) {
            LocalDate startYear = LocalDate.of(nowJakarta.getYear(), 1, 1);
            LocalDate endYear = LocalDate.of(nowJakarta.getYear(), 12, 31);
            start = startYear.atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            end = endYear.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } else {
            // Default: 30 hari terakhir
            LocalDate today = nowJakarta.toLocalDate();
            start = today.minusDays(30).atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
            end = today.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        }

        List<Map<String, Object>> aggResult = detailPesananRepository.getMenuTerlarisAggregated(
                start, end, category, org.springframework.data.domain.PageRequest.of(0, limit));

        return aggResult.stream().map(row -> {
            UUID menuId = (UUID) row.get("menuId");
            String menuName = (String) row.get("menuName");
            long totalQty = ((Number) row.get("totalQty")).longValue();
            BigDecimal totalIncome = (BigDecimal) row.get("totalIncome");
            return new MenuTerlarisResponse(menuId, menuName, totalQty, totalIncome);
        }).toList();
    }

    @Override
    public List<RatingSentimenResponse> getRatingSentimen() {
        // Inisialisasi map dengan 0 hit untuk 1..5 bintang
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        List<Map<String, Object>> rawData = ratingRepository.getRatingDistributionRaw();
        for (Map<String, Object> row : rawData) {
            Number bintangVal = (Number) row.get("bintang");
            Number countVal = (Number) row.get("count");
            if (bintangVal != null && countVal != null) {
                distribution.put(bintangVal.intValue(), countVal.longValue());
            }
        }

        return distribution.entrySet().stream()
                .map(e -> new RatingSentimenResponse(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(RatingSentimenResponse::bintang).reversed())
                .toList();
    }

    @Override
    public PoinPromoStatsResponse getPoinPromoStats() {
        // 1. Total point redeemed & nilai rupiahnya
        long totalPoinRedeemed = poinTransaksiRepository.getTotalPointsRedeemed();
        BigDecimal totalNilaiRedeemRupiah = BigDecimal.valueOf(totalPoinRedeemed).multiply(BigDecimal.valueOf(rupiahPerPoin));

        // 2. Diskon promo & total pesanan pakai promo
        BigDecimal totalDiskonPromo = detailPesananRepository.getTotalDiskonPromo();
        long totalPesananPakaiPromo = detailPesananRepository.getTotalPesananPakaiPromo();

        List<Map<String, Object>> promoStats = detailPesananRepository.getPromoUsageStatsAggregated();
        PoinPromoStatsResponse.TopPromoResponse topPromo = null;

        if (!promoStats.isEmpty()) {
            Map<String, Object> topRow = promoStats.get(0);
            UUID promoId = (UUID) topRow.get("promoId");
            String namaPromo = (String) topRow.get("namaPromo");
            long count = ((Number) topRow.get("count")).longValue();
            topPromo = new PoinPromoStatsResponse.TopPromoResponse(promoId, namaPromo, count);
        }

        return new PoinPromoStatsResponse(
                totalPoinRedeemed,
                totalNilaiRedeemRupiah,
                totalDiskonPromo,
                totalPesananPakaiPromo,
                topPromo
        );
    }

    @Override
    public byte[] exportLaporan(String period, String format) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Style headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Sheet 1: Dashboard Ringkasan
            Sheet sheet1 = workbook.createSheet("Ringkasan");
            DashboardStatsResponse stats = getDashboardStats();

            Row row0 = sheet1.createRow(0);
            row0.createCell(0).setCellValue("Metrik Dashboard Harian");
            row0.getCell(0).setCellStyle(headerStyle);

            String[] metrics = {
                    "Pendapatan Hari Ini", "Total Pesanan Hari Ini", "Total Meja Aktif",
                    "Rata-rata Rating Hari Ini", "Average Order Value", "Total Poin Ditebus", "Total Diskon Promo"
            };
            Object[] values = {
                    stats.pendapatanHariIni(), stats.totalPesananHariIni(), stats.totalMejaAktif(),
                    stats.avgRatingHariIni(), stats.avgOrderValue(), stats.totalPoinRedeemed(), stats.totalDiskonPromo()
            };

            for (int i = 0; i < metrics.length; i++) {
                Row row = sheet1.createRow(i + 1);
                row.createCell(0).setCellValue(metrics[i]);
                if (values[i] instanceof Number) {
                    row.createCell(1).setCellValue(((Number) values[i]).doubleValue());
                } else if (values[i] instanceof BigDecimal) {
                    row.createCell(1).setCellValue(((BigDecimal) values[i]).doubleValue());
                }
            }

            // Sheet 2: Tren Pendapatan
            Sheet sheet2 = workbook.createSheet("Tren Pendapatan");
            List<PendapatanTrendResponse> trend = getPendapatanTrend(period, null, null);
            Row headerRow2 = sheet2.createRow(0);
            headerRow2.createCell(0).setCellValue("Periode/Tanggal");
            headerRow2.createCell(1).setCellValue("Total Pendapatan");
            headerRow2.createCell(2).setCellValue("Jumlah Pesanan");
            headerRow2.getCell(0).setCellStyle(headerStyle);
            headerRow2.getCell(1).setCellStyle(headerStyle);
            headerRow2.getCell(2).setCellStyle(headerStyle);

            int rIdx2 = 1;
            for (PendapatanTrendResponse t : trend) {
                Row r = sheet2.createRow(rIdx2++);
                r.createCell(0).setCellValue(t.label());
                r.createCell(1).setCellValue(t.totalPendapatan().doubleValue());
                r.createCell(2).setCellValue(t.totalPesanan());
            }

            // Sheet 3: Menu Terlaris
            Sheet sheet3 = workbook.createSheet("Menu Terlaris");
            List<MenuTerlarisResponse> menus = getMenuTerlaris(period, null, 10);
            Row headerRow3 = sheet3.createRow(0);
            headerRow3.createCell(0).setCellValue("Nama Menu");
            headerRow3.createCell(1).setCellValue("Total Terjual");
            headerRow3.createCell(2).setCellValue("Total Pendapatan");
            headerRow3.getCell(0).setCellStyle(headerStyle);
            headerRow3.getCell(1).setCellStyle(headerStyle);
            headerRow3.getCell(2).setCellStyle(headerStyle);

            int rIdx3 = 1;
            for (MenuTerlarisResponse m : menus) {
                Row r = sheet3.createRow(rIdx3++);
                r.createCell(0).setCellValue(m.menuName());
                r.createCell(1).setCellValue(m.totalTerjual());
                r.createCell(2).setCellValue(m.totalPendapatan().doubleValue());
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            log.error("Gagal men-generate laporan excel", e);
            throw new BusinessException("Gagal men-generate file Excel");
        }
    }

    private LocalDateTime[] getTodayRangeJakarta() {
        ZonedDateTime nowJakarta = ZonedDateTime.now(JAKARTA_ZONE);
        LocalDate todayJakarta = nowJakarta.toLocalDate();
        LocalDateTime start = todayJakarta.atStartOfDay().atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = todayJakarta.atTime(LocalTime.MAX).atZone(JAKARTA_ZONE).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        return new LocalDateTime[]{start, end};
    }

}
