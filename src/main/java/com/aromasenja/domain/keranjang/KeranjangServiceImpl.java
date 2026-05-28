package com.aromasenja.domain.keranjang;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KeranjangServiceImpl implements KeranjangService {

    private final KeranjangRepository keranjangRepository;
    private final DetailKeranjangRepository detailKeranjangRepository;
    private final ClientRepository clientRepository;
    private final MenuRepository menuRepository;
    private final KeranjangMapper keranjangMapper;

    @Override
    @Transactional(readOnly = true)
    public KeranjangResponse getKeranjang(UserPrincipal currentUser) {
        Keranjang keranjang = getOrCreateKeranjangEntity(currentUser);
        return keranjangMapper.toResponse(keranjang);
    }

    @Override
    @Transactional
    public KeranjangResponse addItem(AddKeranjangItemRequest request, UserPrincipal currentUser) {
        Menu menu = menuRepository.findByMenuIdAndIsActiveTrue(request.menuId())
            .orElseThrow(() -> new ResourceNotFoundException("Menu tidak ditemukan"));

        if (!menu.isAvailable()) {
            throw new BusinessException("Menu '" + menu.getMenuName() + "' sedang tidak tersedia");
        }

        Keranjang keranjang = getOrCreateKeranjangEntity(currentUser);

        // Cari apakah item dengan menu tersebut sudah ada di keranjang
        Optional<DetailKeranjang> existingItem = keranjang.getDetailKeranjang().stream()
            .filter(item -> item.getMenu().getMenuId().equals(request.menuId()))
            .findFirst();

        if (existingItem.isPresent()) {
            DetailKeranjang item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.quantity());
            if (request.catatan() != null) {
                item.setCatatan(request.catatan());
            }
            detailKeranjangRepository.save(item);
        } else {
            DetailKeranjang item = new DetailKeranjang();
            item.setKeranjang(keranjang);
            item.setMenu(menu);
            item.setQuantity(request.quantity());
            item.setCatatan(request.catatan());
            detailKeranjangRepository.save(item);
            keranjang.getDetailKeranjang().add(item);
        }

        Keranjang savedKeranjang = keranjangRepository.save(keranjang);
        return keranjangMapper.toResponse(savedKeranjang);
    }

    @Override
    @Transactional
    public KeranjangResponse updateItem(UUID detailId, UpdateKeranjangItemRequest request, UserPrincipal currentUser) {
        Keranjang keranjang = getOrCreateKeranjangEntity(currentUser);

        DetailKeranjang item = keranjang.getDetailKeranjang().stream()
            .filter(i -> i.getDetailKeranjangId().equals(detailId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Item keranjang tidak ditemukan atau bukan milik Anda"));

        if (request.quantity() == 0) {
            keranjang.getDetailKeranjang().remove(item);
            detailKeranjangRepository.delete(item);
        } else {
            item.setQuantity(request.quantity());
            item.setCatatan(request.catatan());
            detailKeranjangRepository.save(item);
        }

        Keranjang savedKeranjang = keranjangRepository.save(keranjang);
        return keranjangMapper.toResponse(savedKeranjang);
    }

    @Override
    @Transactional
    public KeranjangResponse removeItem(UUID detailId, UserPrincipal currentUser) {
        Keranjang keranjang = getOrCreateKeranjangEntity(currentUser);

        DetailKeranjang item = keranjang.getDetailKeranjang().stream()
            .filter(i -> i.getDetailKeranjangId().equals(detailId))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Item keranjang tidak ditemukan atau bukan milik Anda"));

        keranjang.getDetailKeranjang().remove(item);
        detailKeranjangRepository.delete(item);

        Keranjang savedKeranjang = keranjangRepository.save(keranjang);
        return keranjangMapper.toResponse(savedKeranjang);
    }

    @Override
    @Transactional
    public void clearKeranjang(UserPrincipal currentUser) {
        Keranjang keranjang = getOrCreateKeranjangEntity(currentUser);
        keranjang.getDetailKeranjang().clear();
        keranjangRepository.save(keranjang);
    }

    private Keranjang getOrCreateKeranjangEntity(UserPrincipal currentUser) {
        if (currentUser.isGuest()) {
            UUID sessionId = currentUser.getUserId();
            return keranjangRepository.findBySessionIdWithDetails(sessionId)
                .orElseGet(() -> {
                    Keranjang newCart = new Keranjang();
                    newCart.setSessionId(sessionId);
                    return keranjangRepository.save(newCart);
                });
        } else {
            Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));

            return keranjangRepository.findByClientIdWithDetails(client.getClientId())
                .orElseGet(() -> {
                    Keranjang newCart = new Keranjang();
                    newCart.setClient(client);
                    return keranjangRepository.save(newCart);
                });
        }
    }
}
