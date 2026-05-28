package com.aromasenja.domain.menu;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ConflictException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.menu.dto.*;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.menu.entity.MenuPairing;
import com.aromasenja.domain.menu.entity.MenuPairingId;
import com.aromasenja.domain.promo.PromoRepository;
import com.aromasenja.domain.promo.entity.Promo;
import com.aromasenja.domain.user.AdminRepository;
import com.aromasenja.domain.user.entity.Admin;
import com.aromasenja.notification.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuServiceImpl Unit Tests")
class MenuServiceImplTest {

    @Mock private MenuRepository menuRepository;
    @Mock private MenuPairingRepository menuPairingRepository;
    @Mock private PromoRepository promoRepository;
    @Mock private AdminRepository adminRepository;
    @Mock private MenuMapper menuMapper;
    @Mock private NotificationService notificationService;

    @InjectMocks
    private MenuServiceImpl menuService;

    private UUID menuId;
    private UUID adminUUID;
    private UUID userId;
    private Menu mockMenu;
    private Admin mockAdmin;
    private UserPrincipal adminPrincipal;

    @BeforeEach
    void setUp() {
        menuId = UUID.randomUUID();
        adminUUID = UUID.randomUUID();
        userId = UUID.randomUUID();

        mockMenu = new Menu();
        mockMenu.setMenuId(menuId);
        mockMenu.setMenuName("Kopi Tubruk");
        mockMenu.setPrice(BigDecimal.valueOf(15000));
        mockMenu.setCategory("Minuman");
        mockMenu.setAvailable(true);
        mockMenu.setActive(true);

        mockAdmin = new Admin();
        mockAdmin.setAdminId(adminUUID);

        adminPrincipal = UserPrincipal.fromClaims(userId, Role.ADMIN);
    }

    @Test
    @DisplayName("getAllActiveMenus — sukses mengembalikan katalog")
    void getAllActiveMenus_sukses() {
        when(menuRepository.findActiveMenusWithPromo(any(), any(), any())).thenReturn(List.of(mockMenu));
        when(menuMapper.toResponseList(anyList())).thenReturn(List.of(new MenuResponse(
            menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, "Minuman", true, null, null
        )));

        List<MenuResponse> result = menuService.getAllActiveMenus("Minuman", null, true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).menuName()).isEqualTo("Kopi Tubruk");
        verify(menuRepository).findActiveMenusWithPromo("Minuman", null, true);
    }

    @Test
    @DisplayName("getMenuDetail — sukses mengembalikan detail menu beserta averageRating")
    void getMenuDetail_sukses() {
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.of(mockMenu));
        when(menuRepository.getAverageRatingForMenu(menuId)).thenReturn(4.5);
        when(menuMapper.toDetailResponse(mockMenu)).thenReturn(new MenuDetailResponse(
            menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, "Minuman", true, null, null, null, null, null, null, null, null, null, null, null, 0.0
        ));

        MenuDetailResponse response = menuService.getMenuDetail(menuId);

        assertThat(response).isNotNull();
        assertThat(response.averageRating()).isEqualTo(4.5);
        verify(menuRepository).findByMenuIdAndIsActiveTrue(menuId);
        verify(menuRepository).getAverageRatingForMenu(menuId);
    }

    @Test
    @DisplayName("getMenuDetail — gagal ketika menu tidak ditemukan")
    void getMenuDetail_gagal_tidakDitemukan() {
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.getMenuDetail(menuId))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getPairings — sukses mengembalikan daftar pairing menu")
    void getPairings_sukses() {
        when(menuRepository.existsById(menuId)).thenReturn(true);
        Menu pairingMenu = new Menu();
        pairingMenu.setMenuId(UUID.randomUUID());
        pairingMenu.setMenuName("Roti Bakar");

        MenuPairing pairing = new MenuPairing();
        pairing.setPairingMenu(pairingMenu);

        when(menuPairingRepository.findPairingsByMenuId(menuId)).thenReturn(List.of(pairing));
        when(menuMapper.toResponseList(anyList())).thenReturn(List.of(new MenuResponse(
            pairingMenu.getMenuId(), "Roti Bakar", BigDecimal.valueOf(12000), null, "Makanan", true, null, null
        )));

        List<MenuResponse> result = menuService.getPairings(menuId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).menuName()).isEqualTo("Roti Bakar");
    }

    @Test
    @DisplayName("createMenu — sukses membuat menu baru")
    void createMenu_sukses() {
        CreateMenuRequest request = new CreateMenuRequest(
            "Kopi Tubruk", BigDecimal.valueOf(15000), "Kopi hitam manis", "Minuman", null, null, null, null, null, null, false, Collections.emptyList(), Collections.emptyList()
        );

        when(menuRepository.existsByMenuNameAndCategoryAndIsActiveTrue("Kopi Tubruk", "Minuman")).thenReturn(false);
        when(adminRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockAdmin));
        when(menuRepository.save(any(Menu.class))).thenReturn(mockMenu);
        when(menuRepository.findByMenuIdAndIsActiveTrue(any())).thenReturn(Optional.of(mockMenu));
        when(menuMapper.toDetailResponse(any())).thenReturn(new MenuDetailResponse(
            menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, "Minuman", true, null, null, null, null, null, null, null, null, null, null, null, 0.0
        ));

        MenuDetailResponse response = menuService.createMenu(request, adminPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.menuName()).isEqualTo("Kopi Tubruk");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("createMenu — gagal ketika nama menu duplikat di kategori yang sama")
    void createMenu_gagal_namaDuplikat() {
        CreateMenuRequest request = new CreateMenuRequest(
            "Kopi Tubruk", BigDecimal.valueOf(15000), null, "Minuman", null, null, null, null, null, null, false, null, null
        );

        when(menuRepository.existsByMenuNameAndCategoryAndIsActiveTrue("Kopi Tubruk", "Minuman")).thenReturn(true);

        assertThatThrownBy(() -> menuService.createMenu(request, adminPrincipal))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("sudah ada di kategori");
    }

    @Test
    @DisplayName("toggleAvailability — sukses mengubah status ketersediaan dan memicu WebSocket")
    void toggleAvailability_sukses() {
        UpdateMenuAvailabilityRequest request = new UpdateMenuAvailabilityRequest(false);
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.of(mockMenu));
        when(menuRepository.save(any(Menu.class))).thenReturn(mockMenu);
        when(menuMapper.toDetailResponse(any())).thenReturn(new MenuDetailResponse(
            menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, "Minuman", false, null, null, null, null, null, null, null, null, null, null, null, 0.0
        ));

        MenuDetailResponse response = menuService.toggleAvailability(menuId, request);

        assertThat(response).isNotNull();
        assertThat(response.isAvailable()).isFalse();
        verify(notificationService).publishMenuAvailability(any(MenuAvailabilityWsPayload.class));
    }

    @Test
    @DisplayName("softDeleteMenu — sukses ketika menu tidak ada di pesanan aktif")
    void softDeleteMenu_sukses() {
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.of(mockMenu));
        when(menuRepository.isMenuInActiveOrders(menuId)).thenReturn(false);

        menuService.softDeleteMenu(menuId);

        verify(menuRepository).softDelete(menuId);
    }

    @Test
    @DisplayName("softDeleteMenu — gagal melempar ConflictException ketika menu ada di pesanan aktif")
    void softDeleteMenu_gagal_adaDiPesananAktif() {
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.of(mockMenu));
        when(menuRepository.isMenuInActiveOrders(menuId)).thenReturn(true);

        assertThatThrownBy(() -> menuService.softDeleteMenu(menuId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("sedang ada di pesanan aktif");

        verify(menuRepository, never()).softDelete(any());
    }
}
