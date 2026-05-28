package com.aromasenja.domain.menu;

import com.aromasenja.domain.menu.dto.MenuDetailResponse;
import com.aromasenja.domain.menu.dto.MenuResponse;
import com.aromasenja.domain.menu.entity.Menu;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:02:39+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class MenuMapperImpl implements MenuMapper {

    @Override
    public MenuResponse toResponse(Menu menu) {
        if ( menu == null ) {
            return null;
        }

        UUID menuId = null;
        String menuName = null;
        BigDecimal price = null;
        String description = null;
        String category = null;
        String imageUrl = null;
        MenuResponse.PromoMinResponse promo = null;

        menuId = menu.getMenuId();
        menuName = menu.getMenuName();
        price = menu.getPrice();
        description = menu.getDescription();
        category = menu.getCategory();
        imageUrl = menu.getImageUrl();
        promo = toPromoMinResponse( menu.getPromo() );

        boolean isAvailable = false;

        MenuResponse menuResponse = new MenuResponse( menuId, menuName, price, description, category, isAvailable, imageUrl, promo );

        return menuResponse;
    }

    @Override
    public List<MenuResponse> toResponseList(List<Menu> menus) {
        if ( menus == null ) {
            return null;
        }

        List<MenuResponse> list = new ArrayList<MenuResponse>( menus.size() );
        for ( Menu menu : menus ) {
            list.add( toResponse( menu ) );
        }

        return list;
    }

    @Override
    public MenuDetailResponse toDetailResponse(Menu menu) {
        if ( menu == null ) {
            return null;
        }

        UUID menuId = null;
        String menuName = null;
        BigDecimal price = null;
        String description = null;
        String category = null;
        String imageUrl = null;
        UUID createdBy = null;
        UUID updatedBy = null;
        MenuDetailResponse.PromoDetailResponse promo = null;
        String titleLine1 = null;
        String titleLine2 = null;
        String longDescription = null;
        String heroImageUrl = null;
        Boolean showDoneness = null;
        List<String> donenessOptions = null;
        List<String> spiceOptions = null;

        menuId = menu.getMenuId();
        menuName = menu.getMenuName();
        price = menu.getPrice();
        description = menu.getDescription();
        category = menu.getCategory();
        imageUrl = menu.getImageUrl();
        createdBy = menu.getCreatedBy();
        updatedBy = menu.getUpdatedBy();
        promo = toPromoDetailResponse( menu.getPromo() );
        titleLine1 = menu.getTitleLine1();
        titleLine2 = menu.getTitleLine2();
        longDescription = menu.getLongDescription();
        heroImageUrl = menu.getHeroImageUrl();
        showDoneness = menu.getShowDoneness();
        List<String> list = menu.getDonenessOptions();
        if ( list != null ) {
            donenessOptions = new ArrayList<String>( list );
        }
        List<String> list1 = menu.getSpiceOptions();
        if ( list1 != null ) {
            spiceOptions = new ArrayList<String>( list1 );
        }

        double averageRating = 0.0d;
        boolean isAvailable = false;

        MenuDetailResponse menuDetailResponse = new MenuDetailResponse( menuId, menuName, price, description, category, isAvailable, imageUrl, createdBy, updatedBy, promo, titleLine1, titleLine2, longDescription, heroImageUrl, showDoneness, donenessOptions, spiceOptions, averageRating );

        return menuDetailResponse;
    }
}
