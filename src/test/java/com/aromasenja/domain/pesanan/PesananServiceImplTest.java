package com.aromasenja.domain.pesanan;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.config_resto.RestoConfigRepository;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.domain.meja.MejaRepository;
import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.meja.entity.ZoneMeja;
import com.aromasenja.domain.keranjang.KeranjangRepository;
import com.aromasenja.domain.keranjang.entity.DetailKeranjang;
import com.aromasenja.domain.keranjang.entity.Keranjang;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.pesanan.dto.*;
import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.promo.entity.Promo;
import com.aromasenja.domain.promo.entity.TipeDiskon;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.User;
import com.aromasenja.domain.poin.PoinTransaksiRepository;
import com.aromasenja.notification.NotificationService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
@DisplayName("PesananServiceImpl Unit Tests")
class PesananServiceImplTest {

    @Mock private PesananRepository pesananRepository;
    @Mock private DetailPesananRepository detailPesananRepository;
    @Mock private KeranjangRepository keranjangRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private MejaRepository mejaRepository;
    @Mock private PoinTransaksiRepository poinTransaksiRepository;
    @Mock private RestoConfigRepository restoConfigRepository;
    @Mock private NotificationService notificationService;
    @Mock private PesananMapper pesananMapper;

    @InjectMocks
    private PesananServiceImpl pesananService;

    private UUID pesananId;
    private UUID mejaId;
    private UUID userId;
    private UUID clientId;
    private UserPrincipal clientPrincipal;
    private UserPrincipal adminPrincipal;
    private UserPrincipal guestPrincipal;

    private RestoConfig restoConfig;
    private Meja meja;
    private Client client;
    private User user;
    private Keranjang keranjang;
    private Menu menu;
    private Pesanan pesanan;
    private PesananResponse mockResponse;

    @BeforeEach
    void setUp() {
        pesananId = UUID.randomUUID();
        mejaId = UUID.randomUUID();
        userId = UUID.randomUUID();
        clientId = UUID.randomUUID();

        clientPrincipal = UserPrincipal.fromClaims(userId, Role.CLIENT);
        adminPrincipal = UserPrincipal.fromClaims(UUID.randomUUID(), Role.ADMIN);
        guestPrincipal = UserPrincipal.forGuest(UUID.randomUUID(), mejaId);

        restoConfig = new RestoConfig();
        restoConfig.setOpen(true);

        meja = new Meja();
        meja.setMejaId(mejaId);
        meja.setNomorMeja(5);
        meja.setZone(ZoneMeja.INDOOR);
        meja.setActive(true);
        meja.setOccupied(false);

        user = new User();
        user.setId(userId);

        client = new Client();
        client.setClientId(clientId);
        client.setUser(user);
        client.setTotalPoint(10); // 10 point = Rp 10.000 diskon

        menu = new Menu();
        menu.setMenuId(UUID.randomUUID());
        menu.setMenuName("Nasi Goreng");
        menu.setPrice(BigDecimal.valueOf(25000));
        menu.setAvailable(true);
        menu.setActive(true);

        DetailKeranjang dk = new DetailKeranjang();
        dk.setMenu(menu);
        dk.setQuantity(2); // Subtotal 50.000

        keranjang = new Keranjang();
        keranjang.setDetailKeranjang(new ArrayList<>(List.of(dk)));

        pesanan = new Pesanan();
        pesanan.setPesananId(pesananId);
        pesanan.setKodePesanan("AR-20260528-1111");
        pesanan.setMeja(meja);
        pesanan.setClient(client);
        pesanan.setTotalHarga(BigDecimal.valueOf(40000));
        pesanan.setStatus(StatusPesanan.NEW);

        mockResponse = new PesananResponse(
                pesananId, "AR-20260528-1111", LocalDateTime.now(), false,
                BigDecimal.valueOf(40000), null, StatusPesanan.NEW, null, 0, null, 10,
                BigDecimal.valueOf(10000), 5, mejaId, clientId, Collections.emptyList()
        );

        org.springframework.test.util.ReflectionTestUtils.setField(pesananService, "rupiahPerPoin", BigDecimal.valueOf(1000));
        org.springframework.test.util.ReflectionTestUtils.setField(pesananService, "rupiahPerEarnedPoin", BigDecimal.valueOf(10000));
    }

    @Test
    @DisplayName("createPesanan — sukses untuk member dan menggunakan poin")
    void createPesanan_member_denganPoin_sukses() {
        when(restoConfigRepository.findFirstBy()).thenReturn(Optional.of(restoConfig));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(client));
        when(keranjangRepository.findByClientIdWithDetails(clientId)).thenReturn(Optional.of(keranjang));
        when(mejaRepository.findById(mejaId)).thenReturn(Optional.of(meja));
        when(pesananRepository.save(any(Pesanan.class))).thenReturn(pesanan);
        when(pesananMapper.toResponse(any())).thenReturn(mockResponse);

        CreatePesananRequest request = new CreatePesananRequest(mejaId, "Sangat pedas", true);

        PesananResponse response = pesananService.createPesanan(request, clientPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.pesananId()).isEqualTo(pesananId);
        assertThat(client.getTotalPoint()).isEqualTo(0); // Poin 10 terpakai semua
        verify(pesananRepository).save(any(Pesanan.class));
        verify(mejaRepository).save(meja);
        verify(poinTransaksiRepository).save(any());
        verify(notificationService).publishPesananBaru(any());
        verify(notificationService).publishMejaStatus(any());
    }

    @Test
    @DisplayName("createPesanan — gagal jika restoran tutup")
    void createPesanan_gagal_restoTutup() {
        restoConfig.setOpen(false);
        when(restoConfigRepository.findFirstBy()).thenReturn(Optional.of(restoConfig));

        CreatePesananRequest request = new CreatePesananRequest(mejaId, null, false);

        assertThatThrownBy(() -> pesananService.createPesanan(request, clientPrincipal))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Restoran sedang tutup");
    }

    @Test
    @DisplayName("getPesananDetail — sukses bagi client pemilik")
    void getPesananDetail_client_sukses() {
        when(pesananRepository.findByIdWithDetails(pesananId)).thenReturn(Optional.of(pesanan));
        when(pesananMapper.toResponse(pesanan)).thenReturn(mockResponse);

        PesananResponse response = pesananService.getPesananDetail(pesananId, clientPrincipal);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("getPesananDetail — gagal bagi client lain (unauthorized)")
    void getPesananDetail_clientLain_gagal() {
        UserPrincipal clientLain = UserPrincipal.fromClaims(UUID.randomUUID(), Role.CLIENT);
        when(pesananRepository.findByIdWithDetails(pesananId)).thenReturn(Optional.of(pesanan));

        assertThatThrownBy(() -> pesananService.getPesananDetail(pesananId, clientLain))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("getPesananDetail — sukses bagi guest di meja yang sama")
    void getPesananDetail_guest_mejaSama_sukses() {
        when(pesananRepository.findByIdWithDetails(pesananId)).thenReturn(Optional.of(pesanan));
        when(pesananMapper.toResponse(pesanan)).thenReturn(mockResponse);

        PesananResponse response = pesananService.getPesananDetail(pesananId, guestPrincipal);

        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("updateStatus — sukses dari NEW ke PREPARING")
    void updateStatus_preparinge_sukses() {
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));
        when(pesananRepository.save(any(Pesanan.class))).thenReturn(pesanan);
        when(pesananMapper.toResponse(pesanan)).thenReturn(mockResponse);

        UpdateStatusPesananRequest request = new UpdateStatusPesananRequest(StatusPesanan.PREPARING, 15);

        PesananResponse response = pesananService.updateStatus(pesananId, request);

        assertThat(response).isNotNull();
        assertThat(pesanan.getStatus()).isEqualTo(StatusPesanan.PREPARING);
        assertThat(pesanan.getEstimasiMenit()).isEqualTo(15);
        verify(notificationService).publishStatusPesanan(eq(pesananId), any());
    }

    @Test
    @DisplayName("bayarPesanan — sukses melunasi dan mengkreditkan poin")
    void bayarPesanan_sukses() {
        pesanan.setTotalHarga(BigDecimal.valueOf(50000));
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));
        when(pesananRepository.save(any(Pesanan.class))).thenReturn(pesanan);
        when(pesananMapper.toResponse(pesanan)).thenReturn(mockResponse);

        BayarPesananRequest request = new BayarPesananRequest(MetodePembayaran.QRIS, BigDecimal.valueOf(50000));

        PesananResponse response = pesananService.bayarPesanan(pesananId, request);

        assertThat(response).isNotNull();
        assertThat(pesanan.getStatus()).isEqualTo(StatusPesanan.SERVED);
        assertThat(client.getTotalPoint()).isEqualTo(15); // Earned 5 points (50k / 10k)
        verify(poinTransaksiRepository).save(any());
        verify(notificationService).publishStatusPesanan(eq(pesananId), any());
    }

    @Test
    @DisplayName("cancelPesanan — sukses membatalkan dan mengembalikan poin")
    void cancelPesanan_sukses() {
        pesanan.setPoinDigunakan(10);
        pesanan.setPotonganPoin(BigDecimal.valueOf(10000));
        when(pesananRepository.findById(pesananId)).thenReturn(Optional.of(pesanan));
        when(pesananRepository.save(any(Pesanan.class))).thenReturn(pesanan);

        pesananService.cancelPesanan(pesananId);

        assertThat(pesanan.getStatus()).isEqualTo(StatusPesanan.CANCELLED);
        assertThat(client.getTotalPoint()).isEqualTo(20); // 10 poin dibalikkan
        assertThat(meja.isOccupied()).isFalse();
        verify(mejaRepository).save(meja);
        verify(notificationService).publishMejaStatus(any());
        verify(notificationService).publishStatusPesanan(eq(pesananId), any());
    }
}
