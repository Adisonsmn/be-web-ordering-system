package com.aromasenja.domain.promo;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.domain.pesanan.DetailPesananRepository;
import com.aromasenja.domain.promo.dto.CreatePromoRequest;
import com.aromasenja.domain.promo.dto.PromoResponse;
import com.aromasenja.domain.promo.entity.Promo;
import com.aromasenja.domain.promo.entity.TipeDiskon;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.promo.dto.PromoHistoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PromoServiceImpl Unit Tests")
class PromoServiceImplTest {

    @Mock private PromoRepository promoRepository;
    @Mock private DetailPesananRepository detailPesananRepository;
    @Mock private PesananRepository pesananRepository;
    @Mock private PromoMapper promoMapper;

    @InjectMocks
    private PromoServiceImpl promoService;

    private UUID promoId;
    private Promo mockPromo;
    private CreatePromoRequest createRequest;
    private PromoResponse mockResponse;

    @BeforeEach
    void setUp() {
        promoId = UUID.randomUUID();
        mockPromo = new Promo();
        mockPromo.setPromoId(promoId);
        mockPromo.setNamaPromo("Diskon Senja");
        mockPromo.setTipeDiskon(TipeDiskon.NOMINAL);
        mockPromo.setNilaiDiskon(BigDecimal.valueOf(5000));
        mockPromo.setTanggalMulai(LocalDate.now().minusDays(1));
        mockPromo.setTanggalSelesai(LocalDate.now().plusDays(5));
        mockPromo.setActive(true);

        createRequest = new CreatePromoRequest(
                "Diskon Senja",
                TipeDiskon.NOMINAL,
                BigDecimal.valueOf(5000),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null, null, null, null, null
        );

        mockResponse = new PromoResponse(
                promoId,
                "Diskon Senja",
                TipeDiskon.NOMINAL,
                BigDecimal.valueOf(5000),
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null,
                true,
                null, null, null, null, null
        );
    }

    @Test
    @DisplayName("getActivePromosForClient — sukses")
    void getActivePromosForClient_sukses() {
        when(promoRepository.findByIsActiveTrueAndTanggalMulaiLessThanEqualAndTanggalSelesaiGreaterThanEqual(any(), any()))
                .thenReturn(List.of(mockPromo));
        when(promoMapper.toResponseList(anyList())).thenReturn(List.of(mockResponse));

        List<PromoResponse> result = promoService.getActivePromosForClient();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).namaPromo()).isEqualTo("Diskon Senja");
    }

    @Test
    @DisplayName("getAllPromosForAdmin — status active")
    void getAllPromosForAdmin_active() {
        when(promoRepository.findActivePromos(any())).thenReturn(List.of(mockPromo));
        when(promoMapper.toResponseList(anyList())).thenReturn(List.of(mockResponse));

        List<PromoResponse> result = promoService.getAllPromosForAdmin("active");

        assertThat(result).hasSize(1);
        verify(promoRepository).findActivePromos(any());
    }

    @Test
    @DisplayName("createPromo — sukses")
    void createPromo_sukses() {
        when(promoMapper.toEntity(any(CreatePromoRequest.class))).thenReturn(mockPromo);
        when(promoRepository.save(any(Promo.class))).thenReturn(mockPromo);
        when(promoMapper.toResponse(any(Promo.class))).thenReturn(mockResponse);

        PromoResponse result = promoService.createPromo(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.namaPromo()).isEqualTo("Diskon Senja");
    }

    @Test
    @DisplayName("createPromo — gagal ketika tanggal selesai sebelum mulai")
    void createPromo_gagal_tanggalSelesaiSebelumMulai() {
        CreatePromoRequest invalid = new CreatePromoRequest(
                "Promo Gagal", TipeDiskon.NOMINAL, BigDecimal.ONE,
                LocalDate.now().plusDays(1), LocalDate.now(), null, null, null, null, null
        );

        assertThatThrownBy(() -> promoService.createPromo(invalid))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tanggal selesai tidak boleh sebelum");
    }

    @Test
    @DisplayName("updatePromo — sukses ketika tidak ada relasi order")
    void updatePromo_sukses() {
        when(promoRepository.findById(promoId)).thenReturn(Optional.of(mockPromo));
        when(detailPesananRepository.existsByMenuPromoPromoId(promoId)).thenReturn(false);
        when(promoRepository.save(any(Promo.class))).thenReturn(mockPromo);
        when(promoMapper.toResponse(any(Promo.class))).thenReturn(mockResponse);

        PromoResponse result = promoService.updatePromo(promoId, createRequest);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("updatePromo — gagal ketika sudah digunakan di order dan nilai diskon diubah")
    void updatePromo_gagal_nilaiDiskonDiubahKetikaSudahDigunakan() {
        when(promoRepository.findById(promoId)).thenReturn(Optional.of(mockPromo));
        when(detailPesananRepository.existsByMenuPromoPromoId(promoId)).thenReturn(true);

        CreatePromoRequest updateRequest = new CreatePromoRequest(
                "Diskon Senja",
                TipeDiskon.NOMINAL,
                BigDecimal.valueOf(10000), // Diubah dari 5000
                LocalDate.now().minusDays(1),
                LocalDate.now().plusDays(5),
                null, null, null, null, null
        );

        assertThatThrownBy(() -> promoService.updatePromo(promoId, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("yang sudah digunakan tidak boleh diubah");
    }

    @Test
    @DisplayName("deletePromo — sukses (soft delete)")
    void deletePromo_sukses() {
        when(promoRepository.findById(promoId)).thenReturn(Optional.of(mockPromo));
        when(promoRepository.save(any(Promo.class))).thenReturn(mockPromo);

        promoService.deletePromo(promoId);

        assertThat(mockPromo.isActive()).isFalse();
        verify(promoRepository).save(mockPromo);
    }

    @Test
    @DisplayName("getPromoHistory — sukses")
    void getPromoHistory_sukses() {
        when(promoRepository.findById(promoId)).thenReturn(Optional.of(mockPromo));
        Pageable pageable = PageRequest.of(0, 10);
        Page<com.aromasenja.domain.pesanan.entity.Pesanan> page = new PageImpl<>(Collections.emptyList());
        when(pesananRepository.findByPromoId(promoId, pageable)).thenReturn(page);

        Page<PromoHistoryResponse> result = promoService.getPromoHistory(promoId, pageable);

        assertThat(result).isNotNull();
        verify(pesananRepository).findByPromoId(promoId, pageable);
    }
}
