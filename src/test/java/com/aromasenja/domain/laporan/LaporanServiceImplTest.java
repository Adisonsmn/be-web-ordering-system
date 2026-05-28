package com.aromasenja.domain.laporan;

import com.aromasenja.domain.meja.MejaRepository;
import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.pesanan.PesananMapper;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.pesanan.DetailPesananRepository;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.poin.PoinTransaksiRepository;
import com.aromasenja.domain.rating.RatingRepository;
import com.aromasenja.domain.laporan.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("LaporanServiceImpl Unit Tests")
class LaporanServiceImplTest {

    @Mock private PesananRepository pesananRepository;
    @Mock private RatingRepository ratingRepository;
    @Mock private MejaRepository mejaRepository;
    @Mock private PoinTransaksiRepository poinTransaksiRepository;
    @Mock private DetailPesananRepository detailPesananRepository;
    @Mock private PesananMapper pesananMapper;

    @InjectMocks
    private LaporanServiceImpl laporanService;

    @BeforeEach
    void setUp() {
        org.springframework.test.util.ReflectionTestUtils.setField(laporanService, "rupiahPerPoin", 100);
    }

    @Test
    @DisplayName("Get Dashboard Stats - Success")
    void getDashboardStats_Success() {
        when(pesananRepository.sumTotalHargaByStatusAndTanggalPesananBetween(eq(StatusPesanan.SERVED), any(), any()))
                .thenReturn(BigDecimal.valueOf(150000));
        when(pesananRepository.countByTanggalPesananBetween(any(), any())).thenReturn(5L);
        when(mejaRepository.countByIsActiveTrueAndIsOccupiedTrue()).thenReturn(0L);
        when(ratingRepository.getAverageRatingByCreatedAtBetween(any(), any())).thenReturn(4.2);
        when(pesananRepository.findByTanggalPesananBetween(any(), any())).thenReturn(Collections.emptyList());
        when(pesananRepository.findByStatusIn(any())).thenReturn(Collections.emptyList());
        when(detailPesananRepository.getTotalDiskonPromoBetween(any(), any())).thenReturn(BigDecimal.ZERO);

        DashboardStatsResponse response = laporanService.getDashboardStats();

        assertThat(response).isNotNull();
        assertThat(response.pendapatanHariIni()).isEqualByComparingTo("150000");
        assertThat(response.totalPesananHariIni()).isEqualTo(5L);
        assertThat(response.avgRatingHariIni()).isEqualTo(4.2);
    }

    @Test
    @DisplayName("Get Pendapatan Trend - Success Bulanan")
    void getPendapatanTrend_Bulanan_Success() {
        when(pesananRepository.findByTanggalPesananBetween(any(), any())).thenReturn(Collections.emptyList());

        List<PendapatanTrendResponse> response = laporanService.getPendapatanTrend("bulanan", 5, 2026);

        assertThat(response).isNotNull();
        assertThat(response).hasSize(31); // Bulan Mei memiliki 31 hari
        assertThat(response.get(0).totalPendapatan()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Get Rating Sentimen - Success")
    void getRatingSentimen_Success() {
        Map<String, Object> row = new HashMap<>();
        row.put("bintang", 5);
        row.put("count", 12L);
        when(ratingRepository.getRatingDistributionRaw()).thenReturn(Collections.singletonList(row));

        List<RatingSentimenResponse> response = laporanService.getRatingSentimen();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(5); // 1..5 bintang
        RatingSentimenResponse fiveStar = response.stream()
                .filter(r -> r.bintang() == 5)
                .findFirst()
                .orElse(null);
        assertThat(fiveStar).isNotNull();
        assertThat(fiveStar.count()).isEqualTo(12L);
    }

    @Test
    @DisplayName("Export Laporan to Excel - Success")
    void exportLaporan_Success() {
        when(pesananRepository.sumTotalHargaByStatusAndTanggalPesananBetween(eq(StatusPesanan.SERVED), any(), any()))
                .thenReturn(BigDecimal.valueOf(150000));
        when(pesananRepository.countByTanggalPesananBetween(any(), any())).thenReturn(5L);
        when(mejaRepository.countByIsActiveTrueAndIsOccupiedTrue()).thenReturn(0L);
        when(ratingRepository.getAverageRatingByCreatedAtBetween(any(), any())).thenReturn(4.2);
        when(pesananRepository.findByTanggalPesananBetween(any(), any())).thenReturn(Collections.emptyList());
        when(pesananRepository.findByStatusIn(any())).thenReturn(Collections.emptyList());
        when(detailPesananRepository.getTotalDiskonPromoBetween(any(), any())).thenReturn(BigDecimal.ZERO);
        when(detailPesananRepository.getMenuTerlarisAggregated(any(), any(), any(), any())).thenReturn(Collections.emptyList());

        byte[] bytes = laporanService.exportLaporan("bulanan", "xlsx");

        assertThat(bytes).isNotEmpty();
        assertThat(bytes.length).isGreaterThan(100); // Excel header standard size
    }
}
