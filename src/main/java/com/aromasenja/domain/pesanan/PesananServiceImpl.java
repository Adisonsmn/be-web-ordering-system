package com.aromasenja.domain.pesanan;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.config_resto.RestoConfigRepository;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.domain.meja.MejaRepository;
import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.keranjang.KeranjangRepository;
import com.aromasenja.domain.keranjang.entity.DetailKeranjang;
import com.aromasenja.domain.keranjang.entity.Keranjang;
import com.aromasenja.domain.pesanan.dto.*;
import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.promo.entity.Promo;
import com.aromasenja.domain.promo.entity.TipeDiskon;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.poin.PoinTransaksiRepository;
import com.aromasenja.domain.poin.entity.PoinTransaksi;
import com.aromasenja.domain.poin.entity.TipePoin;
import com.aromasenja.notification.NotificationService;
import com.aromasenja.notification.payload.MejaStatusWsPayload;
import com.aromasenja.notification.payload.PesananBaruWsPayload;
import com.aromasenja.notification.payload.PesananStatusWsPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PesananServiceImpl implements PesananService {

    private final PesananRepository pesananRepository;
    private final DetailPesananRepository detailPesananRepository;
    private final KeranjangRepository keranjangRepository;
    private final ClientRepository clientRepository;
    private final MejaRepository mejaRepository;
    private final PoinTransaksiRepository poinTransaksiRepository;
    private final RestoConfigRepository restoConfigRepository;
    private final NotificationService notificationService;
    private final PesananMapper pesananMapper;

    @Value("${app.poin.rupiah-per-poin}")
    private BigDecimal rupiahPerPoin;

    @Value("${app.poin.rupiah-per-earned-poin}")
    private BigDecimal rupiahPerEarnedPoin;

    @Override
    public PesananResponse createPesanan(CreatePesananRequest request, UserPrincipal currentUser) {
        // 1. Validasi Restoran
        RestoConfig config = restoConfigRepository.findFirstBy()
                .orElseThrow(() -> new BusinessException("Konfigurasi restoran tidak ditemukan"));
        if (!config.isOpen()) {
            throw new BusinessException("Restoran sedang tutup");
        }

        // 2. Ambil Keranjang
        Keranjang keranjang = getKeranjangEntity(currentUser);
        if (keranjang.getDetailKeranjang().isEmpty()) {
            throw new BusinessException("Keranjang belanja kosong");
        }

        // 3. Validasi Meja
        Meja meja = mejaRepository.findById(request.mejaId())
                .orElseThrow(() -> new ResourceNotFoundException("Meja tidak ditemukan"));
        if (!meja.isActive()) {
            throw new BusinessException("Meja tidak aktif");
        }

        // 4. Inisiasi Pesanan
        Pesanan pesanan = new Pesanan();
        pesanan.setKodePesanan(generateKodePesanan());
        pesanan.setMeja(meja);
        pesanan.setStatus(StatusPesanan.NEW);
        pesanan.setCatatanDapur(request.catatanDapur());

        Client client = null;
        if (!currentUser.isGuest()) {
            client = clientRepository.findByUser_Id(currentUser.getUserId())
                    .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));
            pesanan.setClient(client);
        }

        // 5. Ubah Detail Keranjang -> Detail Pesanan & Snapshot Harga
        BigDecimal subtotalOrder = BigDecimal.ZERO;
        List<DetailPesanan> details = new ArrayList<>();
        int totalQuantity = 0;

        for (DetailKeranjang dk : keranjang.getDetailKeranjang()) {
            if (!dk.getMenu().isActive() || !dk.getMenu().isAvailable()) {
                throw new BusinessException("Menu '" + dk.getMenu().getMenuName() + "' sedang tidak tersedia");
            }

            DetailPesanan dp = new DetailPesanan();
            dp.setPesanan(pesanan);
            dp.setMenu(dk.getMenu());
            dp.setQuantity(dk.getQuantity());
            dp.setCatatan(dk.getCatatan());

            BigDecimal originalPrice = dk.getMenu().getPrice();
            dp.setHargaSnapshot(originalPrice);

            // Cek promo aktif
            BigDecimal discountedPrice = originalPrice;
            Promo promo = dk.getMenu().getPromo();
            if (isPromoActive(promo)) {
                if (promo.getTipeDiskon() == TipeDiskon.NOMINAL) {
                    discountedPrice = originalPrice.subtract(promo.getNilaiDiskon()).max(BigDecimal.ZERO);
                } else if (promo.getTipeDiskon() == TipeDiskon.PERSEN) {
                    BigDecimal percentage = promo.getNilaiDiskon().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal discount = originalPrice.multiply(percentage);
                    discountedPrice = originalPrice.subtract(discount).max(BigDecimal.ZERO);
                }
            }
            dp.setHargaSetelahDiskon(discountedPrice);

            BigDecimal subtotalItem = discountedPrice.multiply(BigDecimal.valueOf(dk.getQuantity()));
            dp.setSubTotal(subtotalItem);

            subtotalOrder = subtotalOrder.add(subtotalItem);
            totalQuantity += dk.getQuantity();
            details.add(dp);
        }
        pesanan.setDetailPesanan(details);

        // 6. Logika Poin
        int poinDigunakan = 0;
        BigDecimal potonganPoin = BigDecimal.ZERO;
        if (Boolean.TRUE.equals(request.gunakanPoin()) && client != null && client.getTotalPoint() > 0) {
            BigDecimal totalPointVal = BigDecimal.valueOf(client.getTotalPoint());
            BigDecimal potentialDiscount = totalPointVal.multiply(rupiahPerPoin);

            if (potentialDiscount.compareTo(subtotalOrder) >= 0) {
                // Point mencakup seluruh subtotal
                poinDigunakan = subtotalOrder.divide(rupiahPerPoin, 0, RoundingMode.DOWN).intValue();
                potonganPoin = BigDecimal.valueOf(poinDigunakan).multiply(rupiahPerPoin);
            } else {
                poinDigunakan = client.getTotalPoint();
                potonganPoin = potentialDiscount;
            }

            pesanan.setPoinDigunakan(poinDigunakan);
            pesanan.setPotonganPoin(potonganPoin);

            // Deduct client point
            client.setTotalPoint(client.getTotalPoint() - poinDigunakan);
            clientRepository.save(client);
        }

        BigDecimal totalHarga = subtotalOrder.subtract(potonganPoin).max(BigDecimal.ZERO);
        pesanan.setTotalHarga(totalHarga);

        // 7. Simpan Pesanan & Update Status Meja
        Pesanan savedPesanan = pesananRepository.save(pesanan);

        meja.setOccupied(true);
        mejaRepository.save(meja);

        // Record point redemption jika poin digunakan
        if (poinDigunakan > 0 && client != null) {
            PoinTransaksi redemption = new PoinTransaksi();
            redemption.setClient(client);
            redemption.setPesanan(savedPesanan);
            redemption.setJumlahPoin(poinDigunakan);
            redemption.setTipe(TipePoin.REDEEM);
            poinTransaksiRepository.save(redemption);
        }

        // 8. Kosongkan Keranjang
        keranjang.getDetailKeranjang().clear();
        keranjangRepository.save(keranjang);

        // 9. WebSocket Broadcasts
        try {
            notificationService.publishPesananBaru(new PesananBaruWsPayload(
                    savedPesanan.getPesananId(),
                    savedPesanan.getKodePesanan(),
                    meja.getNomorMeja(),
                    meja.getZone().name(),
                    savedPesanan.getTotalHarga(),
                    totalQuantity,
                    savedPesanan.getTanggalPesanan()
            ));

            notificationService.publishMejaStatus(new MejaStatusWsPayload(
                    meja.getMejaId(),
                    meja.getNomorMeja(),
                    true
            ));
        } catch (Exception e) {
            log.error("Gagal mengirimkan notifikasi WebSocket untuk pesanan baru: {}", savedPesanan.getPesananId(), e);
        }

        return pesananMapper.toResponse(savedPesanan);
    }

    @Override
    @Transactional(readOnly = true)
    public PesananResponse getPesananDetail(UUID pesananId, UserPrincipal currentUser) {
        Pesanan pesanan = pesananRepository.findByIdWithDetails(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        validateAccess(pesanan, currentUser);

        return pesananMapper.toResponse(pesanan);
    }

    @Override
    @Transactional(readOnly = true)
    public StrukPesananResponse getStruk(UUID pesananId, UserPrincipal currentUser) {
        Pesanan pesanan = pesananRepository.findByIdWithDetails(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        validateAccess(pesanan, currentUser);

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal diskonPromo = BigDecimal.ZERO;

        List<StrukPesananResponse.StrukItem> items = new ArrayList<>();
        for (DetailPesanan dp : pesanan.getDetailPesanan()) {
            BigDecimal itemSubtotal = dp.getHargaSnapshot().multiply(BigDecimal.valueOf(dp.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            BigDecimal promoDiscountPerItem = dp.getHargaSnapshot().subtract(dp.getHargaSetelahDiskon());
            BigDecimal itemPromoDiscount = promoDiscountPerItem.multiply(BigDecimal.valueOf(dp.getQuantity()));
            diskonPromo = diskonPromo.add(itemPromoDiscount);

            items.add(new StrukPesananResponse.StrukItem(
                    dp.getMenu().getMenuName(),
                    dp.getQuantity(),
                    dp.getHargaSetelahDiskon(),
                    dp.getSubTotal(),
                    dp.getCatatan()
            ));
        }

        return new StrukPesananResponse(
                pesanan.getPesananId(),
                pesanan.getKodePesanan(),
                pesanan.getTanggalPesanan(),
                pesanan.getMeja().getNomorMeja(),
                pesanan.getMetodePembayaran(),
                subtotal,
                pesanan.getPotonganPoin(),
                diskonPromo,
                pesanan.getTotalHarga(),
                items
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PesananResponse> getRiwayatPesanan(UserPrincipal currentUser, Pageable pageable) {
        if (currentUser.isGuest()) {
            throw new UnauthorizedException("Guest tidak memiliki riwayat pesanan permanen");
        }

        Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));

        Page<Pesanan> page = pesananRepository.findByClientClientIdOrderByTanggalPesananDesc(client.getClientId(), pageable);
        return page.map(pesananMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PesananResponse> getAllPesananAdmin(StatusPesanan status, UUID mejaId, LocalDate tanggal, Pageable pageable) {
        Page<Pesanan> page = pesananRepository.findAllAdminFiltered(status, mejaId, tanggal, pageable);
        return page.map(pesananMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public KanbanPesananResponse getKanbanPesananAdmin() {
        List<Pesanan> newOrders = pesananRepository.findTop20ByStatusOrderByTanggalPesananAsc(StatusPesanan.NEW);
        List<Pesanan> preparingOrders = pesananRepository.findTop20ByStatusOrderByTanggalPesananAsc(StatusPesanan.PREPARING);
        List<Pesanan> readyOrders = pesananRepository.findTop20ByStatusOrderByTanggalPesananAsc(StatusPesanan.READY);

        return new KanbanPesananResponse(
                newOrders.stream().map(pesananMapper::toResponse).toList(),
                preparingOrders.stream().map(pesananMapper::toResponse).toList(),
                readyOrders.stream().map(pesananMapper::toResponse).toList()
        );
    }

    @Override
    public PesananResponse updateStatus(UUID pesananId, UpdateStatusPesananRequest request) {
        Pesanan pesanan = pesananRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        if (pesanan.getStatus() == StatusPesanan.CANCELLED || pesanan.getStatus() == StatusPesanan.SERVED) {
            throw new BusinessException("Status pesanan yang sudah selesai atau dibatalkan tidak bisa diperbarui");
        }

        pesanan.setStatus(request.status());
        if (request.status() == StatusPesanan.PREPARING && request.estimasiMenit() != null) {
            pesanan.setEstimasiMenit(request.estimasiMenit());
        }

        if (request.status() == StatusPesanan.SERVED) {
            pesanan.setServed(true);
            
            // Kosongkan meja
            Meja meja = pesanan.getMeja();
            if (meja != null) {
                meja.setOccupied(false);
                mejaRepository.save(meja);
                
                try {
                    notificationService.publishMejaStatus(new MejaStatusWsPayload(
                            meja.getMejaId(),
                            meja.getNomorMeja(),
                            false
                    ));
                } catch (Exception e) {
                    log.error("Gagal publish WS meja status untuk pesanan SERVED: mejaId={}", meja.getMejaId(), e);
                }
            }
        }

        Pesanan updated = pesananRepository.save(pesanan);

        // WS Broadcast ke client
        try {
            notificationService.publishStatusPesanan(pesananId, new PesananStatusWsPayload(
                    updated.getPesananId(),
                    updated.getStatus().toDbValue(),
                    updated.getStatus() == StatusPesanan.PREPARING ? updated.getEstimasiMenit() : null,
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Gagal publish WS update status pesanan: {}", pesananId, e);
        }

        return pesananMapper.toResponse(updated);
    }

    @Override
    public PesananResponse bayarPesanan(UUID pesananId, BayarPesananRequest request) {
        Pesanan pesanan = pesananRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        if (pesanan.getStatus() == StatusPesanan.CANCELLED) {
            throw new BusinessException("Pesanan yang sudah dibatalkan tidak bisa dibayar");
        }

        if (pesanan.getMetodePembayaran() != null) {
            throw new BusinessException("Pesanan sudah dibayar sebelumnya");
        }

        if (request.jumlahDibayar().compareTo(pesanan.getTotalHarga()) < 0) {
            throw new BusinessException("Jumlah dibayar kurang dari total harga");
        }

        pesanan.setMetodePembayaran(request.metodePembayaran());
        pesanan.setJumlahDibayar(request.jumlahDibayar());
        pesanan.setStatus(StatusPesanan.SERVED);
        pesanan.setServed(true);

        // Kosongkan meja
        Meja meja = pesanan.getMeja();
        if (meja != null) {
            meja.setOccupied(false);
            mejaRepository.save(meja);
            
            try {
                notificationService.publishMejaStatus(new MejaStatusWsPayload(
                        meja.getMejaId(),
                        meja.getNomorMeja(),
                        false
                ));
            } catch (Exception e) {
                log.error("Gagal publish WS meja status untuk pembayaran pesanan: mejaId={}", meja.getMejaId(), e);
            }
        }

        Pesanan saved = pesananRepository.save(pesanan);

        // Kredit poin ke client jika terdaftar (bukan guest)
        Client client = pesanan.getClient();
        if (client != null) {
            int earnedPoints = saved.getTotalHarga().divide(rupiahPerEarnedPoin, 0, RoundingMode.DOWN).intValue();
            if (earnedPoints > 0) {
                client.setTotalPoint(client.getTotalPoint() + earnedPoints);
                clientRepository.save(client);

                PoinTransaksi earning = new PoinTransaksi();
                earning.setClient(client);
                earning.setPesanan(saved);
                earning.setJumlahPoin(earnedPoints);
                earning.setTipe(TipePoin.EARN);
                poinTransaksiRepository.save(earning);
            }
        }

        // WS Broadcast ke client & status meja ke admin
        try {
            notificationService.publishStatusPesanan(pesananId, new PesananStatusWsPayload(
                    saved.getPesananId(),
                    saved.getStatus().toDbValue(),
                    null,
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Gagal publish WS update status pembayaran pesanan: {}", pesananId, e);
        }

        return pesananMapper.toResponse(saved);
    }

    @Override
    public void cancelPesanan(UUID pesananId) {
        Pesanan pesanan = pesananRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        if (pesanan.getStatus() == StatusPesanan.SERVED || pesanan.getStatus() == StatusPesanan.READY) {
            throw new BusinessException("Pesanan tidak bisa dibatalkan karena sudah siap atau disajikan");
        }

        pesanan.setStatus(StatusPesanan.CANCELLED);
        pesananRepository.save(pesanan);

        // Kembalikan poin ke client jika sudah digunakan
        if (pesanan.getPoinDigunakan() > 0 && pesanan.getClient() != null) {
            Client client = pesanan.getClient();
            client.setTotalPoint(client.getTotalPoint() + pesanan.getPoinDigunakan());
            clientRepository.save(client);

            // Catat transaksi refund
            PoinTransaksi refund = new PoinTransaksi();
            refund.setClient(client);
            refund.setPesanan(pesanan);
            refund.setJumlahPoin(pesanan.getPoinDigunakan());
            refund.setTipe(TipePoin.REFUND);
            poinTransaksiRepository.save(refund);
        }

        // Kosongkan status meja terkait
        Meja meja = pesanan.getMeja();
        if (meja != null) {
            meja.setOccupied(false);
            mejaRepository.save(meja);

            try {
                notificationService.publishMejaStatus(new MejaStatusWsPayload(
                        meja.getMejaId(),
                        meja.getNomorMeja(),
                        false
                ));
            } catch (Exception e) {
                log.error("Gagal publish WS cancel meja status: mejaId={}", meja.getMejaId(), e);
            }
        }

        try {
            notificationService.publishStatusPesanan(pesananId, new PesananStatusWsPayload(
                    pesanan.getPesananId(),
                    StatusPesanan.CANCELLED.toDbValue(),
                    null,
                    LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("Gagal publish WS cancel pesanan status: {}", pesananId, e);
        }
    }

    private String generateKodePesanan() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uniquePart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "AR-" + dateStr + "-" + uniquePart;
    }

    private boolean isPromoActive(Promo promo) {
        if (promo == null || !promo.isActive()) return false;
        LocalDate today = LocalDate.now();
        return !today.isBefore(promo.getTanggalMulai()) && !today.isAfter(promo.getTanggalSelesai());
    }

    private Keranjang getKeranjangEntity(UserPrincipal currentUser) {
        if (currentUser.isGuest()) {
            UUID sessionId = currentUser.getUserId();
            return keranjangRepository.findBySessionIdWithDetails(sessionId)
                    .orElseThrow(() -> new BusinessException("Keranjang guest tidak ditemukan"));
        } else {
            Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                    .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));

            return keranjangRepository.findByClientIdWithDetails(client.getClientId())
                    .orElseThrow(() -> new BusinessException("Keranjang member tidak ditemukan"));
        }
    }

    private void validateAccess(Pesanan pesanan, UserPrincipal currentUser) {
        if (currentUser.hasRole("ADMIN")) {
            return;
        }

        if (currentUser.isGuest()) {
            if (currentUser.getTableId().equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
                // Fallback dummy UI: allow access
                return;
            }
            if (pesanan.getMeja() == null || !pesanan.getMeja().getMejaId().equals(currentUser.getTableId())) {
                throw new UnauthorizedException("Anda tidak berhak mengakses pesanan dari meja lain");
            }
        } else {
            if (pesanan.getClient() == null ||
                    pesanan.getClient().getUser() == null ||
                    !pesanan.getClient().getUser().getId().equals(currentUser.getUserId())) {
                throw new UnauthorizedException("Anda tidak berhak mengakses pesanan ini");
            }
        }
    }
}
