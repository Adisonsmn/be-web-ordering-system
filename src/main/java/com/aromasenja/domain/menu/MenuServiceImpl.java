package com.aromasenja.domain.menu;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.ConflictException;
import com.aromasenja.common.exception.ResourceNotFoundException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.menu.dto.*;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.menu.entity.MenuPairing;
import com.aromasenja.domain.promo.PromoRepository;
import com.aromasenja.domain.promo.entity.Promo;
import com.aromasenja.domain.user.AdminRepository;
import com.aromasenja.domain.user.entity.Admin;
import com.aromasenja.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuPairingRepository menuPairingRepository;
    private final PromoRepository promoRepository;
    private final AdminRepository adminRepository;
    private final MenuMapper menuMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getAllActiveMenus(String category, String search, Boolean available) {
        List<Menu> menus = menuRepository.findActiveMenusWithPromo(category, search, available);
        return menuMapper.toResponseList(menus);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuDetailResponse getMenuDetail(UUID menuId) {
        Menu menu = menuRepository.findByMenuIdAndIsActiveTrue(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu tidak ditemukan"));

        double averageRating = menuRepository.getAverageRatingForMenu(menuId);

        MenuDetailResponse response = menuMapper.toDetailResponse(menu);
        // MapStruct copies everything, we just override averageRating
        return new MenuDetailResponse(
            response.menuId(),
            response.menuName(),
            response.price(),
            response.description(),
            response.category(),
            response.isAvailable(),
            response.imageUrl(),
            response.createdBy(),
            response.updatedBy(),
            response.promo(),
            response.titleLine1(),
            response.titleLine2(),
            response.longDescription(),
            response.heroImageUrl(),
            response.showDoneness(),
            response.donenessOptions(),
            response.spiceOptions(),
            averageRating
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuResponse> getPairings(UUID menuId) {
        // Cek dulu menu exists
        if (!menuRepository.existsById(menuId)) {
            throw new ResourceNotFoundException("Menu tidak ditemukan");
        }

        List<MenuPairing> pairings = menuPairingRepository.findPairingsByMenuId(menuId);
        List<Menu> pairingMenus = pairings.stream()
            .map(MenuPairing::getPairingMenu)
            .collect(Collectors.toList());

        return menuMapper.toResponseList(pairingMenus);
    }

    @Override
    @Transactional
    public MenuDetailResponse createMenu(CreateMenuRequest request, UserPrincipal currentUser) {
        if (menuRepository.existsByMenuNameAndCategoryAndIsActiveTrue(request.menuName(), request.category())) {
            throw new BusinessException("Menu dengan nama '" + request.menuName() + "' sudah ada di kategori '" + request.category() + "'");
        }

        UUID adminId = getAdminId(currentUser.getUserId());

        Menu menu = new Menu();
        menu.setMenuName(request.menuName());
        menu.setPrice(request.price());
        menu.setDescription(request.description());
        menu.setCategory(request.category());
        menu.setImageUrl(request.imageUrl());
        menu.setTitleLine1(request.titleLine1());
        menu.setTitleLine2(request.titleLine2());
        menu.setLongDescription(request.longDescription());
        menu.setHeroImageUrl(request.heroImageUrl());
        menu.setShowDoneness(request.showDoneness() != null ? request.showDoneness() : false);
        menu.setDonenessOptions(request.donenessOptions());
        menu.setSpiceOptions(request.spiceOptions());
        menu.setCreatedBy(adminId);
        menu.setUpdatedBy(adminId);

        if (request.promoId() != null) {
            Promo promo = promoRepository.findById(request.promoId())
                .orElseThrow(() -> new ResourceNotFoundException("Promo tidak ditemukan"));
            menu.setPromo(promo);
        }

        Menu savedMenu = menuRepository.save(menu);
        return getMenuDetail(savedMenu.getMenuId());
    }

    @Override
    @Transactional
    public MenuDetailResponse updateMenu(UUID menuId, UpdateMenuRequest request, UserPrincipal currentUser) {
        Menu menu = menuRepository.findByMenuIdAndIsActiveTrue(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu tidak ditemukan"));

        if (!menu.getMenuName().equalsIgnoreCase(request.menuName()) || !menu.getCategory().equalsIgnoreCase(request.category())) {
            if (menuRepository.existsByMenuNameAndCategoryAndIsActiveTrue(request.menuName(), request.category())) {
                throw new BusinessException("Menu dengan nama '" + request.menuName() + "' sudah ada di kategori '" + request.category() + "'");
            }
        }

        UUID adminId = getAdminId(currentUser.getUserId());

        menu.setMenuName(request.menuName());
        menu.setPrice(request.price());
        menu.setDescription(request.description());
        menu.setCategory(request.category());
        menu.setImageUrl(request.imageUrl());
        menu.setTitleLine1(request.titleLine1());
        menu.setTitleLine2(request.titleLine2());
        menu.setLongDescription(request.longDescription());
        menu.setHeroImageUrl(request.heroImageUrl());
        menu.setShowDoneness(request.showDoneness() != null ? request.showDoneness() : false);
        menu.setDonenessOptions(request.donenessOptions());
        menu.setSpiceOptions(request.spiceOptions());
        menu.setUpdatedBy(adminId);

        if (request.promoId() != null) {
            Promo promo = promoRepository.findById(request.promoId())
                .orElseThrow(() -> new ResourceNotFoundException("Promo tidak ditemukan"));
            menu.setPromo(promo);
        } else {
            menu.setPromo(null);
        }

        Menu savedMenu = menuRepository.save(menu);
        return getMenuDetail(savedMenu.getMenuId());
    }

    @Override
    @Transactional
    public MenuDetailResponse toggleAvailability(UUID menuId, UpdateMenuAvailabilityRequest request) {
        Menu menu = menuRepository.findByMenuIdAndIsActiveTrue(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu tidak ditemukan"));

        menu.setAvailable(request.isAvailable());
        menuRepository.save(menu);

        // Broadcast WS event
        notificationService.publishMenuAvailability(new MenuAvailabilityWsPayload(
            menu.getMenuId(),
            menu.getMenuName(),
            menu.isAvailable()
        ));

        return getMenuDetail(menu.getMenuId());
    }

    @Override
    @Transactional
    public void softDeleteMenu(UUID menuId) {
        Menu menu = menuRepository.findByMenuIdAndIsActiveTrue(menuId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu tidak ditemukan"));

        // Validasi: tidak sedang ada di pesanan dengan status aktif (NEW/PREPARING/READY)
        if (menuRepository.isMenuInActiveOrders(menuId)) {
            throw new ConflictException("Menu tidak bisa dihapus karena sedang ada di pesanan aktif");
        }

        menuRepository.softDelete(menuId);
    }

    private UUID getAdminId(UUID userId) {
        return adminRepository.findByUser_Id(userId)
            .map(Admin::getAdminId)
            .orElseThrow(() -> new BusinessException("Profil admin tidak ditemukan"));
    }
}
