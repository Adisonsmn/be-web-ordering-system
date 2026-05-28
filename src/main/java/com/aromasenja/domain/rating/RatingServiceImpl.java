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

    @Override
    public RatingResponse submitRating(CreateRatingRequest request, UserPrincipal currentUser) {
        // 1. Ambil Pesanan
        Pesanan pesanan = pesananRepository.findById(request.pesananId())
                .orElseThrow(() -> new ResourceNotFoundException("Pesanan tidak ditemukan"));

        // 2. Ambil Client & Validasi Kepemilikan
        Client client = null;
        if (!currentUser.isGuest()) {
            client = clientRepository.findByUser_Id(currentUser.getUserId())
                    .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));
            if (pesanan.getClient() == null || !pesanan.getClient().getClientId().equals(client.getClientId())) {
                throw new UnauthorizedException("Kamu tidak berhak memberikan ulasan pada pesanan ini");
            }
        } else {
            // Guest rating validation
            if (currentUser.getTableId().equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
                // Dummy UI fallback, allow access
            } else if (pesanan.getMeja() == null || !pesanan.getMeja().getMejaId().equals(currentUser.getTableId())) {
                throw new UnauthorizedException("Anda tidak berhak mengakses pesanan dari meja lain");
            }
            if (pesanan.getClient() != null) {
                throw new UnauthorizedException("Pesanan ini milik member, tidak bisa di-rating oleh guest");
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

        return ratingMapper.toResponse(savedOverall);
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
                if (pesanan.getMeja() == null || !pesanan.getMeja().getMejaId().equals(currentUser.getTableId())) {
                    throw new UnauthorizedException("Anda tidak berhak mengakses pesanan dari meja lain");
                }
            } else {
                Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                        .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));
                if (pesanan.getClient() == null || !pesanan.getClient().getClientId().equals(client.getClientId())) {
                    throw new UnauthorizedException("Kamu tidak berhak mengakses pesanan ini");
                }
            }
        }

        boolean isRated = ratingRepository.existsByPesananPesananId(pesananId);
        return new PesananRatingStatusResponse(isRated);
    }
}
