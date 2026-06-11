package com.aromasenja.domain.promo;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.domain.pesanan.DetailPesananRepository;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.promo.dto.CreatePromoRequest;
import com.aromasenja.domain.promo.dto.PromoResponse;
import com.aromasenja.domain.promo.dto.PromoHistoryResponse;
import com.aromasenja.domain.promo.entity.Promo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PromoServiceImpl implements PromoService {

    private final PromoRepository promoRepository;
    private final DetailPesananRepository detailPesananRepository;
    private final PesananRepository pesananRepository;
    private final PromoMapper promoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PromoResponse> getActivePromosForClient() {
        LocalDate today = LocalDate.now();
        List<Promo> activePromos = promoRepository
                .findByIsActiveTrueAndTanggalMulaiLessThanEqualAndTanggalSelesaiGreaterThanEqual(today, today);
        return promoMapper.toResponseList(activePromos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PromoResponse> getAllPromosForAdmin(String status) {
        LocalDate today = LocalDate.now();
        List<Promo> promos;

        if ("active".equalsIgnoreCase(status)) {
            promos = promoRepository.findActivePromos(today);
        } else if ("scheduled".equalsIgnoreCase(status)) {
            promos = promoRepository.findScheduledPromos(today);
        } else if ("ended".equalsIgnoreCase(status)) {
            promos = promoRepository.findEndedPromos(today);
        } else {
            promos = promoRepository.findAll();
        }

        return promoMapper.toResponseList(promos);
    }

    @Override
    public PromoResponse createPromo(CreatePromoRequest request) {
        if (request.tanggalSelesai().isBefore(request.tanggalMulai())) {
            throw new BusinessException("Tanggal selesai tidak boleh sebelum tanggal mulai");
        }

        Promo promo = promoMapper.toEntity(request);
        promo.setActive(true);
        Promo savedPromo = promoRepository.save(promo);
        return promoMapper.toResponse(savedPromo);
    }

    @Override
    public PromoResponse updatePromo(UUID promoId, CreatePromoRequest request) {
        Promo promo = promoRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo tidak ditemukan"));

        if (request.tanggalSelesai().isBefore(request.tanggalMulai())) {
            throw new BusinessException("Tanggal selesai tidak boleh sebelum tanggal mulai");
        }

        // Cek jika promo ini sudah digunakan di pesanan
        boolean isUsedInOrders = detailPesananRepository.existsByMenuPromoPromoId(promoId);

        if (isUsedInOrders) {
            // Nilai diskon dan tipe diskon tidak boleh diubah
            if (!promo.getNilaiDiskon().equals(request.nilaiDiskon()) ||
                    promo.getTipeDiskon() != request.tipeDiskon()) {
                throw new BusinessException("Tipe diskon dan nilai diskon promo yang sudah digunakan tidak boleh diubah untuk menjaga konsistensi data");
            }
        }

        promoMapper.updateEntityFromRequest(request, promo);
        Promo updatedPromo = promoRepository.save(promo);
        return promoMapper.toResponse(updatedPromo);
    }

    @Override
    public void deletePromo(UUID promoId) {
        Promo promo = promoRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo tidak ditemukan"));

        // Soft delete
        promo.setActive(false);
        promoRepository.save(promo);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PromoHistoryResponse> getPromoHistory(UUID promoId, Pageable pageable) {
        Promo promo = promoRepository.findById(promoId)
                .orElseThrow(() -> new ResourceNotFoundException("Promo tidak ditemukan"));

        Page<Pesanan> pesananPage = pesananRepository.findByPromoId(promoId, pageable);

        return pesananPage.map(pesanan -> {
            String clientName = (pesanan.getClient() != null && pesanan.getClient().getUser() != null)
                    ? pesanan.getClient().getUser().getName()
                    : "Guest";
            Integer nomorMeja = pesanan.getMeja() != null ? pesanan.getMeja().getNomorMeja() : null;

            BigDecimal totalPotongan = pesanan.getDetailPesanan().stream()
                    .filter(dp -> dp.getMenu().getPromo() != null && dp.getMenu().getPromo().getPromoId().equals(promoId))
                    .map(dp -> dp.getHargaSnapshot().subtract(dp.getHargaSetelahDiskon()).multiply(BigDecimal.valueOf(dp.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new PromoHistoryResponse(
                    pesanan.getPesananId(),
                    pesanan.getKodePesanan(),
                    clientName,
                    nomorMeja,
                    pesanan.getTanggalPesanan(),
                    pesanan.getTotalHarga(),
                    totalPotongan
            );
        });
    }
}
