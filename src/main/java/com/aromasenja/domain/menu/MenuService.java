package com.aromasenja.domain.menu;

import com.aromasenja.domain.menu.dto.*;
import com.aromasenja.common.security.UserPrincipal;
import java.util.List;
import java.util.UUID;

public interface MenuService {
    List<MenuResponse> getAllActiveMenus(String category, String search, Boolean available);
    MenuDetailResponse getMenuDetail(UUID menuId);
    List<MenuResponse> getPairings(UUID menuId);
    MenuDetailResponse createMenu(CreateMenuRequest request, UserPrincipal currentUser);
    MenuDetailResponse updateMenu(UUID menuId, UpdateMenuRequest request, UserPrincipal currentUser);
    MenuDetailResponse toggleAvailability(UUID menuId, UpdateMenuAvailabilityRequest request);
    MenuDetailResponse patchMenuPromo(UUID menuId, UpdateMenuPromoRequest request);
    void softDeleteMenu(UUID menuId);

    /** Ambil menu paling populer (all-time) untuk ditampilkan di WelcomePage customer. */
    MenuResponse getMenuPopuler();
}
