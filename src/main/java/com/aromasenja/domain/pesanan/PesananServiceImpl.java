package com.aromasenja.domain.pesanan;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.config_resto.RestoConfigRepository;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.domain.meja.MejaRepository;
import com.aromasenja.domain.meja.MejaSessionRepository;
import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.keranjang.KeranjangRepository;
import com.aromasenja.domain.keranjang.entity.DetailKeranjang;
import com.aromasenja.domain.keranjang.entity.Keranjang;
import com.aromasenja.domain.pesanan.dto.*;
import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.promo.PromoRepository;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private final PromoRepository promoRepository;
    private final NotificationService notificationService;
    private final PesananMapper pesananMapper;
    private final MejaSessionRepository mejaSessionRepository;

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

        // Kumpulkan promo yang terpakai dalam pesanan ini (distinct per promoId)
        Set<UUID> promoUsedIds = new HashSet<>();
        for (DetailKeranjang dk : keranjang.getDetailKeranjang()) {
            Promo usedPromo = dk.getMenu().getPromo();
            if (isPromoActive(usedPromo)) {
                promoUsedIds.add(usedPromo.getPromoId());
            }
        }

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

        boolean wasOccupied = meja.isOccupied();
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

        // Increment usageCount untuk setiap promo unik yang dipakai dalam pesanan ini
        if (!promoUsedIds.isEmpty()) {
            for (UUID usedPromoId : promoUsedIds) {
                promoRepository.findById(usedPromoId).ifPresent(promo -> {
                    int newCount = promo.getUsageCount() + 1;
                    promo.setUsageCount(newCount);

                    // Auto-deactivate jika sudah mencapai batas penggunaan
                    if (promo.getMaxUsage() != null && newCount >= promo.getMaxUsage()) {
                        promo.setActive(false);
                        log.info("Promo {} otomatis dinonaktifkan karena mencapai batas penggunaan ({}/{})",
                                promo.getNamaPromo(), newCount, promo.getMaxUsage());
                    }

                    promoRepository.save(promo);
                    log.info("Promo {} usageCount diperbarui: {}", promo.getNamaPromo(), newCount);
                });
            }
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

            if (!wasOccupied) {
                notificationService.publishMejaStatus(new MejaStatusWsPayload(
                        meja.getMejaId(),
                        meja.getNomorMeja(),
                        true,
                        "OCCUPIED"
                ));
            }
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
    public Page<PesananResponse> getAllPesananAdmin(StatusPesanan status, List<StatusPesanan> statuses, UUID mejaId, LocalDate tanggal, LocalDateTime startDate, LocalDateTime endDate, String category, Pageable pageable) {
        var spec = PesananSpecification.buildFilter(status, statuses, mejaId, tanggal, startDate, endDate, category);
        Page<Pesanan> page = pesananRepository.findAll(spec, pageable);
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
        if (request.status() == StatusPesanan.PREPARING) {
            if (request.estimasiMenit() == null || request.estimasiMenit() <= 0) {
                throw new BusinessException("Estimasi waktu memasak wajib diisi saat menerima pesanan");
            }
            pesanan.setEstimasiMenit(request.estimasiMenit());
        }

        if (request.status() == StatusPesanan.SERVED) {
            pesanan.setServed(true);
        }

        Pesanan updated = pesananRepository.save(pesanan);

        // Kredit poin ke client saat pesanan selesai (SERVED) — via kanban drag
        if (request.status() == StatusPesanan.SERVED) {
            Client earnClient = updated.getClient();
            if (earnClient != null) {
                // Cegah double-earn jika bayarPesanan sudah pernah dipanggil sebelumnya
                boolean sudahEarn = poinTransaksiRepository
                        .existsByPesananPesananIdAndTipe(updated.getPesananId(), TipePoin.EARN);
                if (!sudahEarn) {
                    int earnedPoints = updated.getTotalHarga()
                            .divide(rupiahPerEarnedPoin, 0, RoundingMode.DOWN).intValue();
                    if (earnedPoints > 0) {
                        earnClient.setTotalPoint(earnClient.getTotalPoint() + earnedPoints);
                        clientRepository.save(earnClient);

                        PoinTransaksi earning = new PoinTransaksi();
                        earning.setClient(earnClient);
                        earning.setPesanan(updated);
                        earning.setJumlahPoin(earnedPoints);
                        earning.setTipe(TipePoin.EARN);
                        poinTransaksiRepository.save(earning);
                        log.info("Poin EARN {} dicredit ke client {} untuk pesanan {}",
                                earnedPoints, earnClient.getClientId(), updated.getPesananId());
                    }
                }
            }
        }

        // WS Broadcast ke client
        try {
            notificationService.publishStatusPesanan(pesananId, new PesananStatusWsPayload(
                    updated.getPesananId(),
                    updated.getStatus().name(),
                    updated.getStatus() == StatusPesanan.PREPARING ? updated.getEstimasiMenit() : null,
                    LocalDateTime.now()
            ));

            // Broadcast ke admin dashboard untuk aktivitas terkini
            notificationService.publishDashboardStats(new java.util.HashMap<>() {{
                put("event", "PESANAN_STATUS_UPDATED");
                put("pesananId", updated.getPesananId());
                put("status", updated.getStatus().name());
                put("nomorMeja", updated.getMeja() != null ? updated.getMeja().getNomorMeja() : null);
                put("kodePesanan", updated.getKodePesanan());
                put("estimasiMenit", updated.getStatus() == StatusPesanan.PREPARING ? updated.getEstimasiMenit() : null);
            }});
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

        // Meja tidak direset otomatis — tamu mungkin masih ada di meja

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
                    saved.getStatus().name(),
                    null,
                    LocalDateTime.now()
            ));

            // Broadcast ke admin dashboard untuk aktivitas terkini
            notificationService.publishDashboardStats(new java.util.HashMap<>() {{
                put("event", "PESANAN_SERVED");
                put("pesananId", saved.getPesananId());
                put("kodePesanan", saved.getKodePesanan());
                put("nomorMeja", saved.getMeja() != null ? saved.getMeja().getNomorMeja() : null);
                put("totalHarga", saved.getTotalHarga());
            }});
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

        // Meja tidak direset otomatis saat cancel

        try {
            notificationService.publishStatusPesanan(pesananId, new PesananStatusWsPayload(
                    pesanan.getPesananId(),
                    StatusPesanan.CANCELLED.name(),
                    null,
                    LocalDateTime.now()
            ));

            // Broadcast ke admin dashboard untuk aktivitas terkini
            notificationService.publishDashboardStats(new java.util.HashMap<>() {{
                put("event", "PESANAN_CANCELLED");
                put("pesananId", pesanan.getPesananId());
                put("kodePesanan", pesanan.getKodePesanan());
                put("nomorMeja", pesanan.getMeja() != null ? pesanan.getMeja().getNomorMeja() : null);
            }});
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
