package com.aromasenja.domain.rating;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.menu.MenuRepository;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.rating.dto.CreateRatingRequest;
import com.aromasenja.domain.rating.dto.MenuRatingResponse;
import com.aromasenja.domain.rating.dto.PesananRatingStatusResponse;
import com.aromasenja.domain.rating.dto.RatingResponse;
import com.aromasenja.domain.rating.entity.Rating;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.entity.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RatingServiceImpl implements RatingService {

    private final RatingRepository ratingRepository;
    private final PesananRepository pesananRepository;
    private final ClientRepository clientRepository;
    private final MenuRepository menuRepository;
    private final RatingMapper ratingMapper;
    private final com.aromasenja.notification.NotificationService notificationService;

    @Override
    public RatingResponse submitRating(CreateRatingRequest request, UserPrincipal currentUser) {
        // 1. Ambil Pesanan
        Pesanan pesanan = pesananRepository.findById(request.pesananId())
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        // 2. Ambil Client & Validasi Kepemilikan
        Client client = null;
        if (currentUser != null) {
            if (currentUser.hasRole("ADMIN")) {
                // Admin can rate anything without a client profile (rates anonymously)
            } else if (!currentUser.isGuest()) {
                client = clientRepository.findByUser_Id(currentUser.getUserId())
                        .orElse(null);
            }
        }

        // 4. Validasi Status Pesanan (Harus SERVED/DONE)
        if (pesanan.getStatus() != StatusPesanan.SERVED) {
            throw new BusinessException("Ulasan hanya dapat diberikan jika pesanan sudah selesai (SERVED)");
        }

        // 5. Validasi: 1 pesanan hanya boleh dirating sekali
        if (ratingRepository.existsByPesananPesananId(request.pesananId())) {
            throw new BusinessException("Pesanan ini sudah diberi ulasan sebelumnya");
        }

        boolean isPublic = request.isPublic() == null || request.isPublic();

        // 6. Simpan Overall Rating
        Rating overallRating = new Rating();
        overallRating.setClient(client);
        overallRating.setPesanan(pesanan);
        overallRating.setBintang(request.ratingOverall().shortValue());
        overallRating.setUlasan(request.ulasanOverall());
        overallRating.setOverall(true);
        overallRating.setPublic(isPublic);
        overallRating.setMenu(null);
        Rating savedOverall = ratingRepository.save(overallRating);

        // 7. Simpan Item Ratings
        if (request.items() != null && !request.items().isEmpty()) {
            // Validasi: Menu-menu yang dirating harus bagian dari pesanan
            List<UUID> orderedMenuIds = pesanan.getDetailPesanan().stream()
                    .map(dp -> dp.getMenu().getMenuId())
                    .toList();

            for (CreateRatingRequest.ItemRatingRequest itemReq : request.items()) {
                if (!orderedMenuIds.contains(itemReq.menuId())) {
                    throw new BusinessException("Menu " + itemReq.menuId() + " tidak ada dalam pesanan ini");
                }

                Menu menu = menuRepository.findById(itemReq.menuId())
                        .orElseThrow(() -> new ResourceNotFoundException("Menu tidak ditemukan"));

                Rating itemRating = new Rating();
                itemRating.setClient(client);
                itemRating.setPesanan(pesanan);
                itemRating.setBintang(itemReq.bintang().shortValue());
                itemRating.setUlasan(itemReq.ulasan());
                itemRating.setOverall(false);
                itemRating.setPublic(isPublic);
                itemRating.setMenu(menu);
                ratingRepository.save(itemRating);
            }
        }

        RatingResponse result = ratingMapper.toResponse(savedOverall);

        // Broadcast ke admin dashboard untuk aktivitas terkini
        try {
            Pesanan finalPesanan = pesanan;
            Client finalClient = client;
            boolean isGuest = (finalClient == null);
            String namaClient = (finalClient != null && finalClient.getUser() != null)
                    ? finalClient.getUser().getName()
                    : null;

            notificationService.publishDashboardStats(new java.util.HashMap<>() {{
                put("event", "RATING_SUBMITTED");
                put("pesananId", finalPesanan.getPesananId());
                put("kodePesanan", finalPesanan.getKodePesanan());
                put("nomorMeja", finalPesanan.getMeja() != null ? finalPesanan.getMeja().getNomorMeja() : null);
                put("bintang", request.ratingOverall());
                put("namaClient", namaClient);
                put("isGuest", isGuest);
            }});
        } catch (Exception e) {
            log.error("Gagal publish WS event rating submitted: pesananId={}", pesanan.getPesananId(), e);
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public MenuRatingResponse getPublicRatingsByMenu(UUID menuId) {
        // Validasi menu exist
        if (!menuRepository.existsById(menuId)) {
            throw new ResourceNotFoundException("Menu tidak ditemukan");
        }

        List<Rating> ratings = ratingRepository.findByMenuMenuIdAndIsPublicTrue(menuId);
        double avg = ratingRepository.getAverageRatingForMenu(menuId);

        List<RatingResponse> responses = ratings.stream()
                .map(ratingMapper::toResponse)
                .collect(Collectors.toList());

        return new MenuRatingResponse(avg, responses);
    }

    @Override
    @Transactional(readOnly = true)
    public PesananRatingStatusResponse checkRatingStatus(UUID pesananId, UserPrincipal currentUser) {
        Pesanan pesanan = pesananRepository.findById(pesananId)
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        // Validasi akses
        if (!currentUser.hasRole("ADMIN")) {
            if (currentUser.isGuest()) {
                // Guest check loosened
            } else {
                Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                        .orElse(null);
                if (client != null && pesanan.getClient() != null && !pesanan.getClient().getClientId().equals(client.getClientId())) {
                    throw new UnauthorizedException("Kamu tidak berhak mengakses pesanan ini");
                }
            }
        }

        boolean isRated = ratingRepository.existsByPesananPesananId(pesananId);
        return new PesananRatingStatusResponse(isRated);
    }
}
