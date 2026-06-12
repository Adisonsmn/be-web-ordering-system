package com.aromasenja.domain.keranjang;

import com.aromasenja.common.Role;
import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.keranjang.dto.*;
import com.aromasenja.domain.keranjang.entity.DetailKeranjang;
import com.aromasenja.domain.keranjang.entity.Keranjang;
import com.aromasenja.domain.menu.MenuRepository;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.entity.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
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
@DisplayName("KeranjangServiceImpl Unit Tests")
class KeranjangServiceImplTest {

    @Mock private KeranjangRepository keranjangRepository;
    @Mock private DetailKeranjangRepository detailKeranjangRepository;
    @Mock private ClientRepository clientRepository;
    @Mock private MenuRepository menuRepository;
    @Mock private KeranjangMapper keranjangMapper;

    @InjectMocks
    private KeranjangServiceImpl keranjangService;

    private UUID cartId;
    private UUID menuId;
    private UUID userId;
    private UUID clientId;
    private UUID sessionId;

    private Menu mockMenu;
    private Client mockClient;
    private Keranjang mockKeranjang;
    private UserPrincipal clientPrincipal;
    private UserPrincipal guestPrincipal;

    @BeforeEach
    void setUp() {
        cartId = UUID.randomUUID();
        menuId = UUID.randomUUID();
        userId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        sessionId = UUID.randomUUID();

        mockMenu = new Menu();
        mockMenu.setMenuId(menuId);
        mockMenu.setMenuName("Kopi Tubruk");
        mockMenu.setPrice(BigDecimal.valueOf(15000));
        mockMenu.setAvailable(true);
        mockMenu.setActive(true);

        mockClient = new Client();
        mockClient.setClientId(clientId);

        mockKeranjang = new Keranjang();
        mockKeranjang.setKeranjangId(cartId);
        mockKeranjang.setDetailKeranjang(new ArrayList<>());

        clientPrincipal = UserPrincipal.fromClaims(userId, Role.CLIENT);
        guestPrincipal = UserPrincipal.forGuest(sessionId, UUID.randomUUID());
    }

    @Test
    @DisplayName("getKeranjang — sukses untuk CLIENT (membuat keranjang baru jika tidak ada)")
    void getKeranjang_client_sukses_baru() {
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));
        when(keranjangRepository.findByClientIdWithDetails(clientId)).thenReturn(Optional.empty());
        when(keranjangRepository.save(any(Keranjang.class))).thenReturn(mockKeranjang);
        when(keranjangMapper.toResponse(any())).thenReturn(new KeranjangResponse(
            cartId, clientId, null, Collections.emptyList(), BigDecimal.ZERO
        ));

        KeranjangResponse response = keranjangService.getKeranjang(clientPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.keranjangId()).isEqualTo(cartId);
        verify(keranjangRepository).save(any(Keranjang.class));
    }

    @Test
    @DisplayName("getKeranjang — sukses untuk GUEST (mengambil keranjang yang sudah ada)")
    void getKeranjang_guest_sukses_exists() {
        mockKeranjang.setSessionId(sessionId);
        when(keranjangRepository.findBySessionIdWithDetails(sessionId)).thenReturn(Optional.of(mockKeranjang));
        when(keranjangMapper.toResponse(mockKeranjang)).thenReturn(new KeranjangResponse(
            cartId, null, sessionId, Collections.emptyList(), BigDecimal.ZERO
        ));

        KeranjangResponse response = keranjangService.getKeranjang(guestPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isEqualTo(sessionId);
        verify(keranjangRepository, never()).save(any());
    }

    @Test
    @DisplayName("addItem — sukses menambahkan item baru ke keranjang")
    void addItem_sukses_baru() {
        AddKeranjangItemRequest request = new AddKeranjangItemRequest(menuId, 2, "Manis");
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.of(mockMenu));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));
        when(keranjangRepository.findByClientIdWithDetails(clientId)).thenReturn(Optional.of(mockKeranjang));
        when(keranjangRepository.save(any(Keranjang.class))).thenReturn(mockKeranjang);
        when(keranjangMapper.toResponse(any())).thenReturn(new KeranjangResponse(
            cartId, clientId, null, List.of(new DetailKeranjangResponse(UUID.randomUUID(), menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, 2, "Manis", BigDecimal.valueOf(30000))), BigDecimal.valueOf(30000)
        ));

        KeranjangResponse response = keranjangService.addItem(request, clientPrincipal);

        assertThat(response).isNotNull();
        assertThat(response.items()).hasSize(1);
        verify(detailKeranjangRepository).save(any(DetailKeranjang.class));
    }

    @Test
    @DisplayName("addItem — sukses mengupdate quantity jika item sudah ada di keranjang")
    void addItem_sukses_updateQuantity() {
        DetailKeranjang existingDetail = new DetailKeranjang();
        existingDetail.setDetailKeranjangId(UUID.randomUUID());
        existingDetail.setMenu(mockMenu);
        existingDetail.setQuantity(2);
        existingDetail.setKeranjang(mockKeranjang);
        mockKeranjang.getDetailKeranjang().add(existingDetail);

        AddKeranjangItemRequest request = new AddKeranjangItemRequest(menuId, 3, "Tambah manis");
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.of(mockMenu));
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));
        when(keranjangRepository.findByClientIdWithDetails(clientId)).thenReturn(Optional.of(mockKeranjang));
        when(keranjangRepository.save(any(Keranjang.class))).thenReturn(mockKeranjang);
        when(keranjangMapper.toResponse(any())).thenReturn(new KeranjangResponse(
            cartId, clientId, null, List.of(new DetailKeranjangResponse(existingDetail.getDetailKeranjangId(), menuId, "Kopi Tubruk", BigDecimal.valueOf(15000), null, 5, "Tambah manis", BigDecimal.valueOf(75000))), BigDecimal.valueOf(75000)
        ));

        KeranjangResponse response = keranjangService.addItem(request, clientPrincipal);

        assertThat(response.items().get(0).quantity()).isEqualTo(5);
        verify(detailKeranjangRepository).save(existingDetail);
    }

    @Test
    @DisplayName("addItem — gagal ketika menu tidak tersedia")
    void addItem_gagal_menuTidakTersedia() {
        mockMenu.setAvailable(false);
        AddKeranjangItemRequest request = new AddKeranjangItemRequest(menuId, 1, null);
        when(menuRepository.findByMenuIdAndIsActiveTrue(menuId)).thenReturn(Optional.of(mockMenu));

        assertThatThrownBy(() -> keranjangService.addItem(request, clientPrincipal))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("sedang tidak tersedia");
    }

    @Test
    @DisplayName("updateItem — sukses mengubah quantity")
    void updateItem_sukses() {
        DetailKeranjang item = new DetailKeranjang();
        UUID detailId = UUID.randomUUID();
        item.setDetailKeranjangId(detailId);
        item.setMenu(mockMenu);
        item.setQuantity(2);
        mockKeranjang.getDetailKeranjang().add(item);

        UpdateKeranjangItemRequest request = new UpdateKeranjangItemRequest(4, "Kurang manis");
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));
        when(keranjangRepository.findByClientIdWithDetails(clientId)).thenReturn(Optional.of(mockKeranjang));
        when(keranjangRepository.save(any(Keranjang.class))).thenReturn(mockKeranjang);

        keranjangService.updateItem(detailId, request, clientPrincipal);

        assertThat(item.getQuantity()).isEqualTo(4);
        assertThat(item.getCatatan()).isEqualTo("Kurang manis");
        verify(detailKeranjangRepository).save(item);
    }

    @Test
    @DisplayName("updateItem — sukses menghapus item jika quantity diset ke 0")
    void updateItem_hapus_quantityNol() {
        DetailKeranjang item = new DetailKeranjang();
        UUID detailId = UUID.randomUUID();
        item.setDetailKeranjangId(detailId);
        item.setMenu(mockMenu);
        item.setQuantity(2);
        mockKeranjang.getDetailKeranjang().add(item);

        UpdateKeranjangItemRequest request = new UpdateKeranjangItemRequest(0, null);
        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));
        when(keranjangRepository.findByClientIdWithDetails(clientId)).thenReturn(Optional.of(mockKeranjang));
        when(keranjangRepository.save(any(Keranjang.class))).thenReturn(mockKeranjang);

        keranjangService.updateItem(detailId, request, clientPrincipal);

        assertThat(mockKeranjang.getDetailKeranjang()).isEmpty();
        verify(detailKeranjangRepository).delete(item);
    }

    @Test
    @DisplayName("clearKeranjang — sukses mengosongkan seluruh item")
    void clearKeranjang_sukses() {
        DetailKeranjang item = new DetailKeranjang();
        item.setMenu(mockMenu);
        item.setQuantity(2);
        mockKeranjang.getDetailKeranjang().add(item);

        when(clientRepository.findByUser_Id(userId)).thenReturn(Optional.of(mockClient));
        when(keranjangRepository.findByClientIdWithDetails(clientId)).thenReturn(Optional.of(mockKeranjang));

        keranjangService.clearKeranjang(clientPrincipal);

        assertThat(mockKeranjang.getDetailKeranjang()).isEmpty();
        verify(keranjangRepository).save(mockKeranjang);
    }
}
