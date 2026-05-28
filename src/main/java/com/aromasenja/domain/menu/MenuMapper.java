package com.aromasenja.domain.menu;

import com.aromasenja.domain.menu.dto.MenuDetailResponse;
import com.aromasenja.domain.menu.dto.MenuResponse;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.promo.entity.Promo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "isAvailable", source = "available")
    MenuResponse toResponse(Menu menu);

    List<MenuResponse> toResponseList(List<Menu> menus);

    @Mapping(target = "averageRating", ignore = true) // Set manually in Service
    @Mapping(target = "isAvailable", source = "available")
    MenuDetailResponse toDetailResponse(Menu menu);

    default MenuResponse.PromoMinResponse toPromoMinResponse(Promo promo) {
        if (promo == null) return null;
        return new MenuResponse.PromoMinResponse(
            promo.getPromoId(),
            promo.getNamaPromo(),
            promo.getTipeDiskon() != null ? promo.getTipeDiskon().name() : null,
            promo.getNilaiDiskon()
        );
    }

    default MenuDetailResponse.PromoDetailResponse toPromoDetailResponse(Promo promo) {
        if (promo == null) return null;
        return new MenuDetailResponse.PromoDetailResponse(
            promo.getPromoId(),
            promo.getNamaPromo(),
            promo.getTipeDiskon() != null ? promo.getTipeDiskon().name() : null,
            promo.getNilaiDiskon(),
            promo.getDescription()
        );
    }
}
