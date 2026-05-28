package com.aromasenja.domain.rating;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.menu.MenuRepository;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.pesanan.PesananRepository;
import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.rating.dto.CreateRatingRequest;
import com.aromasenja.domain.rating.dto.MenuRatingResponse;
import com.aromasenja.domain.rating.dto.PesananRatingStatusResponse;
import com.aromasenja.domain.rating.dto.RatingResponse;
import com.aromasenja.domain.rating.entity.Rating;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RatingServiceImpl Unit Tests")
class RatingServiceImplTest {

    @Mock private RatingRepository ratingRepository;
    @Mock private PesananRepository pesananRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private RatingMapper ratingMapper;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private UUID userId;
    private UUID clientId;
    private UUID pesananId;
    private UUID menuId;
    private UserPrincipal clientPrincipal;
    private UserPrincipal guestPrincipal;

    private Client client;
    private Pesanan pesanan;
    private Menu menu;
    private DetailPesanan detailPesanan;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        pesananId = UUID.randomUUID();
        menuId = UUID.randomUUID();

        clientPrincipal = UserPrincipal.fromClaims(userId, Role.CLIENT);
        guestPrincipal = UserPrincipal.forGuest(UUID.randomUUID(), UUID.randomUUID());

        User user = new User();
        user.setId(userId);
        user.setName("Budi");

        client = new Client();
        client.setClientId(clientId);
        client.setUser(user);

        menu = new Menu();
        menu.setMenuId(menuId);
        menu.setMenuName("Kopi Tubruk");

        detailPesanan = new DetailPesanan();
        detailPesanan.setMenu(menu);
        detailPesanan.setQuantity(1);

        pesanan = new Pesanan();
        pesanan.setPesananId(pesananId);
        pesanan.setClient(client);
        pesanan.setStatus(StatusPesanan.SERVED);
        pesanan.setDetailPesanan(new ArrayList<>(List.of(detailPesanan)));
    }

    /**
     * 
     */
    @Test
    @DisplayName("Submit Rating - Success")
    void submitRating_Success() {
        CreateRatingRequest.ItemRatingRequest itemReq = new CreateRatingRequest.ItemRatingRequest(menuId, (short) 5, "Enak sekali");
        CreateRatingRequest request = new CreateRatingRequest(pesananId, 5, "Pelayanan baik", true, List.of(itemReq));

        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));
        when(ratingRepository.existsByPesananPesananId(pesananId)).thenReturn(false);
        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        Rating mockOverall = new Rating();
        mockOverall.setBintang((short) 5);
        mockOverall.setUlasan("Pelayanan baik");
        mockOverall.setOverall(true);

        when(ratingRepository.save(any(Rating.class))).thenReturn(mockOverall);

        RatingResponse mockRes = new RatingResponse(
                UUID.randomUUID(), clientId, "Budi", null, pesananId, (short) 5, "Pelayanan baik", true, true, null
        );
        when(ratingMapper.toResponse(any(Rating.class))).thenReturn(mockRes);

        RatingResponse response = ratingService.submitRating(request, clientPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.bintang()).isEqualTo(5);
        assertThat(response.ulasan()).isEqualTo("Pelayanan baik");
        verify(ratingRepository, times(2)).save(any(Rating.class));
    }

    @Test
    @DisplayName("Submit Rating - Fail Guest")
    void submitRating_FailGuest() {
        CreateRatingRequest request = new CreateRatingRequest(pesananId, 5, "OK", true, Collections.emptyList());

        assertThatThrownBy(() -> ratingService.submitRating(request, guestPrincipal))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Guest tidak diperbolehkan");
    }

    @Test
    @DisplayName("Submit Rating - Fail Already Rated")
    void submitRating_FailAlreadyRated() {
        CreateRatingRequest request = new CreateRatingRequest(pesananId, 5, "OK", true, Collections.emptyList());

        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));
        when(ratingRepository.existsByPesananPesananId(pesananId)).thenReturn(true);

        assertThatThrownBy(() -> ratingService.submitRating(request, clientPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("sudah diberi ulasan sebelumnya");
    }

    @Test
    @DisplayName("Submit Rating - Fail Pesanan Not Served")
    void submitRating_FailNotServed() {
        pesanan.setStatus(StatusPesanan.NEW);
        CreateRatingRequest request = new CreateRatingRequest(pesananId, 5, "OK", true, Collections.emptyList());

        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));

        assertThatThrownBy(() -> ratingService.submitRating(request, clientPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ulasan hanya dapat diberikan jika pesanan sudah selesai");
    }

    @Test
    @DisplayName("Submit Rating - Fail Menu Not in Order")
    void submitRating_FailMenuNotInOrder() {
        UUID outerMenuId = UUID.randomUUID();
        CreateRatingRequest.ItemRatingRequest itemReq = new CreateRatingRequest.ItemRatingRequest(outerMenuId, (short) 5, "Enak");
        CreateRatingRequest request = new CreateRatingRequest(pesananId, 5, "OK", true, List.of(itemReq));

        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));

        assertThatThrownBy(() -> ratingService.submitRating(request, clientPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tidak ada dalam pesanan ini");
    }

    @Test
    @DisplayName("Get Public Ratings by Menu - Success")
    void getPublicRatingsByMenu_Success() {
        when(menuRepository.existsById(menuId)).thenReturn(true);
        when(ratingRepository.findByMenuMenuIdAndIsPublicTrue(menuId)).thenReturn(Collections.emptyList());
        when(ratingRepository.getAverageRatingForMenu(menuId)).thenReturn(4.5);

        MenuRatingResponse response = ratingService.getPublicRatingsByMenu(menuId);

        assertThat(response).isNotNull();
        assertThat(response.avgRating()).isEqualTo(4.5);
        assertThat(response.ratings()).isEmpty();
    }

    @Test
    @DisplayName("Check Rating Status - Success")
    void checkRatingStatus_Success() {
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));
        when(ratingRepository.existsByPesananPesananId(pesananId)).thenReturn(true);

        PesananRatingStatusResponse response = ratingService.checkRatingStatus(pesananId, clientPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.isRated()).isTrue();
    }
}
