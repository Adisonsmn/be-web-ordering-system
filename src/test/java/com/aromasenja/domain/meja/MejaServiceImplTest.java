package com.aromasenja.domain.meja;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.config_resto.RestoConfigRepository;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import com.aromasenja.domain.meja.dto.CreateMejaRequest;
import com.aromasenja.domain.meja.dto.MejaResponse;
import com.aromasenja.domain.meja.dto.ScanMejaResponse;
import com.aromasenja.domain.meja.dto.UpdateMejaStatusRequest;
import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.meja.entity.ZoneMeja;
import com.aromasenja.domain.user.AdminRepository;
import com.aromasenja.domain.user.entity.Admin;
import com.aromasenja.notification.NotificationService;
import com.aromasenja.notification.payload.MejaStatusWsPayload;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MejaServiceImpl Unit Tests")
class MejaServiceImplTest {

    @Mock private MejaRepository mejaRepository;
    @Mock private MejaMapper mejaMapper;
    @Mock private RestoConfigRepository restoConfigRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private NotificationService notificationService;
    @Mock private MejaSessionRepository mejaSessionRepository;

    @InjectMocks
    private MejaServiceImpl mejaService;

    private UUID mejaId;
    private UUID userId;
    private UUID adminUUID;
    private Meja mockMeja;
    private Admin mockAdmin;

    @BeforeEach
    void setUp() {
        mejaId = UUID.randomUUID();
        userId = UUID.randomUUID();
        adminUUID = UUID.randomUUID();

        mockMeja = new Meja();
        mockMeja.setMejaId(mejaId);
        mockMeja.setNomorMeja(5);
        mockMeja.setZone(ZoneMeja.INDOOR);
        mockMeja.setActive(true);
        mockMeja.setOccupied(false);
        mockMeja.setQrCodeUrl("http://localhost:8080/api/meja/scan/" + mejaId);

        mockAdmin = new Admin();
        mockAdmin.setAdminId(adminUUID);

        ReflectionTestUtils.setField(mejaService, "qrBaseUrl", "http://localhost:8080/api/meja/scan");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getAllMeja — mengembalikan daftar meja aktif yang di-map ke DTO")
    void getAllMeja_returnListMejaAktif() {
        // Arrange
        List<Meja> mejaList = List.of(mockMeja);
        when(mejaRepository.findByIsActiveTrue()).thenReturn(mejaList);
        when(mejaMapper.toResponseList(anyList())).thenAnswer(invocation -> {
            List<Meja> list = invocation.getArgument(0);
            return list.stream().map(m -> new MejaResponse(
                    m.getMejaId(), m.getNomorMeja(), m.getZone().name(), m.isActive(), m.isOccupied(), m.getQrCodeUrl(),
                    m.isOccupied() ? "OCCUPIED" : "AVAILABLE"
            )).toList();
        });

        // Act
        List<MejaResponse> responses = mejaService.getAllMeja();

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).nomorMeja()).isEqualTo(5);
        assertThat(responses.get(0).zone()).isEqualTo("INDOOR");
        verify(mejaRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("createMeja sukses — membuat meja baru, generate QR URL, dan simpan")
    void createMeja_sukses_nomorBaru() {
        // Arrange
        CreateMejaRequest request = new CreateMejaRequest(12, "OUTDOOR");
        when(mejaRepository.findByNomorMeja(12)).thenReturn(Optional.empty());

        // Mock Security Context untuk set created_by
        UserPrincipal principal = UserPrincipal.fromClaims(userId, Role.ADMIN);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(adminRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockAdmin));

        // Mock save
        when(mejaRepository.save(any(Meja.class))).thenAnswer(i -> {
            Meja m = i.getArgument(0);
            if (m.getMejaId() == null) {
                m.setMejaId(UUID.randomUUID());
            }
            return m;
        });

        // Mock Mapper
        when(mejaMapper.toResponse(any(Meja.class))).thenAnswer(i -> {
            Meja m = i.getArgument(0);
            return new MejaResponse(
                    m.getMejaId(), m.getNomorMeja(), m.getZone().name(), m.isActive(), m.isOccupied(), m.getQrCodeUrl(),
                    m.isOccupied() ? "OCCUPIED" : "AVAILABLE"
            );
        });

        // Act
        MejaResponse response = mejaService.createMeja(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.nomorMeja()).isEqualTo(12);
        assertThat(response.zone()).isEqualTo("OUTDOOR");
        assertThat(response.qrCodeUrl()).startsWith("http://localhost:8080/api/meja/scan?meja=");
        verify(mejaRepository, times(2)).save(any(Meja.class));
    }

    @Test
    @DisplayName("createMeja gagal — nomor meja sudah ada melempar BusinessException")
    void createMeja_gagal_nomorSudahAda() {
        // Arrange
        CreateMejaRequest request = new CreateMejaRequest(5, "INDOOR");
        mockMeja.setActive(true);
        when(mejaRepository.findByNomorMeja(5)).thenReturn(Optional.of(mockMeja));

        // Act & Assert
        assertThatThrownBy(() -> mejaService.createMeja(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Nomor meja 5 sudah digunakan");
        verify(mejaRepository, never()).save(any());
    }

    @Test
    @DisplayName("softDeleteMeja sukses — menonaktifkan meja via repository")
    void softDeleteMeja_sukses() {
        // Arrange
        when(mejaRepository.existsById(mejaId)).thenReturn(true);

        // Act
        mejaService.softDeleteMeja(mejaId);

        // Assert
        verify(mejaRepository).softDelete(mejaId);
        verify(mejaSessionRepository).deactivateSessionByMejaId(mejaId);
    }

    @Test
    @DisplayName("softDeleteMeja gagal — meja tidak ditemukan melempar ResourceNotFoundException")
    void softDeleteMeja_gagal_tidakDitemukan() {
        // Arrange
        when(mejaRepository.existsById(mejaId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> mejaService.softDeleteMeja(mejaId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(mejaRepository, never()).softDelete(any());
    }

    @Test
    @DisplayName("generateQrCode sukses — menghasilkan byte array PNG")
    void generateQrCode_sukses_returnByteArrayPng() {
        // Arrange
        when(mejaRepository.findById(mejaId)).thenReturn(Optional.of(mockMeja));

        // Act
        byte[] qrBytes = mejaService.generateQrCode(mejaId);

        // Assert
        assertThat(qrBytes).isNotNull();
        assertThat(qrBytes.length).isPositive();
    }

    @Test
    @DisplayName("generateQrCode gagal — meja tidak aktif melempar BusinessException")
    void generateQrCode_gagal_mejaTidakAktif() {
        // Arrange
        mockMeja.setActive(false);
        when(mejaRepository.findById(mejaId)).thenReturn(Optional.of(mockMeja));

        // Act & Assert
        assertThatThrownBy(() -> mejaService.generateQrCode(mejaId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Tidak bisa generate QR Code untuk meja yang tidak aktif");
    }

    @Test
    @DisplayName("generateQrCode gagal — meja tidak ditemukan melempar ResourceNotFoundException")
    void generateQrCode_gagal_mejaTidakDitemukan() {
        // Arrange
        when(mejaRepository.findById(mejaId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> mejaService.generateQrCode(mejaId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("scanQr sukses — mengembalikan data meja dan status restoConfig")
    void scanQr_sukses_returnInfoMeja() {
        // Arrange
        RestoConfig config = new RestoConfig();
        config.setOpen(true);
        when(mejaRepository.findByIdWithLock(mejaId)).thenReturn(Optional.of(mockMeja));
        when(restoConfigRepository.findFirstBy()).thenReturn(Optional.of(config));
        when(mejaRepository.save(any(Meja.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        ScanMejaResponse response = mejaService.scanQr(mejaId);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.mejaId()).isEqualTo(mejaId);
        assertThat(response.nomorMeja()).isEqualTo(5);
        assertThat(response.isOpen()).isTrue();
    }

    @Test
    @DisplayName("scanQr gagal — meja tidak aktif melempar BusinessException")
    void scanQr_gagal_mejaTidakAktif() {
        // Arrange
        mockMeja.setActive(false);
        when(mejaRepository.findByIdWithLock(mejaId)).thenReturn(Optional.of(mockMeja));

        // Act & Assert
        assertThatThrownBy(() -> mejaService.scanQr(mejaId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Meja tidak aktif atau sudah dihapus");
    }

    @Test
    @DisplayName("updateStatus sukses — mengubah status okupansi dan publish WS event")
    void updateStatus_sukses_triggerWebSocket() {
        // Arrange
        UpdateMejaStatusRequest request = new UpdateMejaStatusRequest(true);
        when(mejaRepository.findById(mejaId)).thenReturn(Optional.of(mockMeja));
        when(mejaRepository.save(any(Meja.class))).thenAnswer(i -> i.getArgument(0));
        when(mejaMapper.toResponse(any(Meja.class))).thenAnswer(i -> {
            Meja m = i.getArgument(0);
            return new MejaResponse(
                    m.getMejaId(), m.getNomorMeja(), m.getZone().name(), m.isActive(), m.isOccupied(), m.getQrCodeUrl(),
                    m.isOccupied() ? "OCCUPIED" : "AVAILABLE"
            );
        });

        // Act
        MejaResponse response = mejaService.updateStatus(mejaId, request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.isOccupied()).isTrue();
        verify(mejaRepository).save(any(Meja.class));
        verify(notificationService).publishMejaStatus(any(MejaStatusWsPayload.class));
    }
}
