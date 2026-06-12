package com.aromasenja.domain.user;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.user.dto.UpdateProfileRequest;
import com.aromasenja.domain.user.dto.UserProfileResponse;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.StatusMember;
import com.aromasenja.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit test untuk UserServiceImpl.
 * Semua dependency di-mock — tidak ada koneksi DB atau Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private ClientRepository clientRepository;

    @InjectMocks
    private UserServiceImpl userService;

    // ── Test fixtures ─────────────────────────────────────────────────────────

    private UUID userId;
    private UUID adminId;
    private User mockClientUser;
    private User mockAdminUser;
    private Client mockClient;
    private UserPrincipal clientPrincipal;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        adminId = UUID.randomUUID();

        mockClientUser = new User();
        mockClientUser.setId(userId);
        mockClientUser.setEmail("client@aromasenja.com");
        mockClientUser.setName("Client User");
        mockClientUser.setPhone("081234567890");
        mockClientUser.setPassword("$2a$encoded");
        mockClientUser.setRole(Role.CLIENT);

        mockAdminUser = new User();
        mockAdminUser.setId(adminId);
        mockAdminUser.setEmail("admin@aromasenja.com");
        mockAdminUser.setName("Admin User");
        mockAdminUser.setRole(Role.ADMIN);

        mockClient = new Client();
        mockClient.setUser(mockClientUser);
        mockClient.setStatusMember(StatusMember.REGULAR);
        mockClient.setTotalPoint(250);

        clientPrincipal = UserPrincipal.from(mockClientUser);
        adminPrincipal = UserPrincipal.from(mockAdminUser);
    }

    // ── getProfile ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getProfile — CLIENT mengembalikan data profil lengkap dengan poin")
    void getProfile_client_returnDataDenganPoin() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockClientUser));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));

        // Act
        UserProfileResponse response = userService.getProfile(clientPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.email()).isEqualTo("client@aromasenja.com");
        assertThat(response.role()).isEqualTo("CLIENT");
        assertThat(response.totalPoint()).isEqualTo(250);
        assertThat(response.statusMember()).isEqualTo("REGULAR");
    }

    @Test
    @DisplayName("getProfile — ADMIN mengembalikan profil tanpa data poin dan statusMember")
    void getProfile_admin_returnDataTanpaPoin() {
        // Arrange
        when(userRepository.findById(adminId)).thenReturn(Optional.of(mockAdminUser));

        // Act
        UserProfileResponse response = userService.getProfile(adminPrincipal);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.role()).isEqualTo("ADMIN");
        assertThat(response.totalPoint()).isNull();
        assertThat(response.statusMember()).isNull();
        // clientRepository tidak boleh dipanggil untuk admin
        verify(clientRepository, never()).findByUser_Id(any());
    }

    @Test
    @DisplayName("getProfile — user tidak ditemukan di DB melempar ResourceNotFoundException")
    void getProfile_userTidakAda_throwNotFound() {
        // Arrange
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getProfile(clientPrincipal))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── updateProfile ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProfile sukses — name, phone, dan avatarUrl tersimpan")
    void updateProfile_sukses_updateNamaDanPhone() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Nama Baru", "089876543210", null
        );
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockClientUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));

        // Act
        UserProfileResponse response = userService.updateProfile(userId, request, clientPrincipal);

        // Assert
        assertThat(response.name()).isEqualTo("Nama Baru");
        assertThat(response.phone()).isEqualTo("089876543210");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile — avatarUrl tersimpan ke entity user")
    void updateProfile_avatarUrl_tersimpan() {
        // Arrange
        String avatarUrl = "https://cdn.aromasenja.com/avatar/user123.jpg";
        UpdateProfileRequest request = new UpdateProfileRequest("Client User", null, avatarUrl);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockClientUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            // Verifikasi avatarUrl sudah diset di entity sebelum di-save
            assertThat(u.getAvatarUrl()).isEqualTo(avatarUrl);
            return u;
        });
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));

        // Act
        UserProfileResponse response = userService.updateProfile(userId, request, clientPrincipal);

        // Assert
        assertThat(response.avatarUrl()).isEqualTo(avatarUrl);
    }

    @Test
    @DisplayName("updateProfile — CLIENT update profil user lain melempar UnauthorizedException")
    void updateProfile_userLain_throwUnauthorized() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        UpdateProfileRequest request = new UpdateProfileRequest("Hacker", null, null);

        // Act & Assert: clientPrincipal (userId) mencoba update otherUserId
        assertThatThrownBy(() -> userService.updateProfile(otherUserId, request, clientPrincipal))
                .isInstanceOf(UnauthorizedException.class);

        // Repository tidak boleh dipanggil karena validasi kepemilikan gagal duluan
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("updateProfile — ADMIN dapat update profil user lain")
    void updateProfile_admin_bisaUpdateUserLain() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Nama Diubah Admin", "081111111111", null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockClientUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));

        // Act — adminPrincipal update profil userId (bukan dirinya sendiri)
        UserProfileResponse response = userService.updateProfile(userId, request, adminPrincipal);

        // Assert
        assertThat(response.name()).isEqualTo("Nama Diubah Admin");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile — user tidak ditemukan melempar ResourceNotFoundException")
    void updateProfile_userTidakAda_throwNotFound() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Nama", null, null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.updateProfile(userId, request, clientPrincipal))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
