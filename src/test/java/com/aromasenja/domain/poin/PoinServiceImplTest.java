package com.aromasenja.domain.poin;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.poin.dto.PoinBalanceResponse;
import com.aromasenja.domain.poin.dto.PoinKalkulasiRequest;
import com.aromasenja.domain.poin.dto.PoinKalkulasiResponse;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import com.aromasenja.domain.poin.entity.PoinTransaksi;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PoinServiceImpl Unit Tests")
class PoinServiceImplTest {

    @Mock private PoinTransaksiRepository poinTransaksiRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private PoinMapper poinMapper;

    @InjectMocks
    private PoinServiceImpl poinService;

    private UUID userId;
    private UserPrincipal clientPrincipal;
    private UserPrincipal guestPrincipal;
    private Client client;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        clientPrincipal = UserPrincipal.fromClaims(userId, Role.CLIENT);
        guestPrincipal = UserPrincipal.forGuest(UUID.randomUUID(), UUID.randomUUID());

        client = new Client();
        client.setClientId(UUID.randomUUID());
        client.setTotalPoint(150);

        org.springframework.test.util.ReflectionTestUtils.setField(poinService, "rupiahPerPoin", 100);
    }

    @Test
    @DisplayName("Get Poin Balance - Success")
    void getPoinBalance_Success() {
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));

        PoinBalanceResponse response = poinService.getPoinBalance(clientPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.totalPoint()).isEqualTo(150);
        assertThat(response.rupiahPerPoin()).isEqualTo(100);
    }

    @Test
    @DisplayName("Get Poin Balance - Fail Guest")
    void getPoinBalance_FailGuest() {
        assertThatThrownBy(() -> poinService.getPoinBalance(guestPrincipal))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Get Riwayat Poin - Success")
    void getRiwayatPoin_Success() {
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));
        Page<PoinTransaksi> emptyPage = new PageImpl<>(Collections.emptyList());
        when(poinTransaksiRepository.findByClientClientIdOrderByCreatedAtDesc(eq(client.getClientId()), any(Pageable.class)))
                .thenReturn(emptyPage);

        Page<PoinRiwayatResponse> response = poinService.getRiwayatPoin(clientPrincipal, Pageable.unpaged());

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Kalkulasi Poin - Success")
    void kalkulasiPoin_Success() {
        PoinKalkulasiRequest request = new PoinKalkulasiRequest(BigDecimal.valueOf(25000), 50);
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));

        PoinKalkulasiResponse response = poinService.kalkulasiPoin(request, clientPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.diskonRupiah()).isEqualByComparingTo("5000"); // 50 * 100
        assertThat(response.totalSetelahDiskon()).isEqualByComparingTo("20000");
    }

    @Test
    @DisplayName("Kalkulasi Poin - Fail Insufficient Points")
    void kalkulasiPoin_FailInsufficientPoints() {
        PoinKalkulasiRequest request = new PoinKalkulasiRequest(BigDecimal.valueOf(25000), 200);
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> poinService.kalkulasiPoin(request, clientPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo poin tidak mencukupi");
    }
}
