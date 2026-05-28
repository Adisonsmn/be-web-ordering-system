package com.aromasenja.domain.keranjang;

import com.aromasenja.domain.keranjang.dto.DetailKeranjangResponse;
import com.aromasenja.domain.keranjang.dto.KeranjangResponse;
import com.aromasenja.domain.keranjang.entity.DetailKeranjang;
import com.aromasenja.domain.keranjang.entity.Keranjang;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.promo.entity.Promo;
import com.aromasenja.domain.promo.entity.TipeDiskon;
import org.mapstruct.Mapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface KeranjangMapper {

    default KeranjangResponse toResponse(Keranjang keranjang) {
        if (keranjang == null) return null;

        List<DetailKeranjangResponse> items = keranjang.getDetailKeranjang().stream()
            .map(this::toDetailResponse)
            .collect(Collectors.toList());

        BigDecimal totalHarga = items.stream()
            .map(DetailKeranjangResponse::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new KeranjangResponse(
            keranjang.getKeranjangId(),
            keranjang.getClient() != null ? keranjang.getClient().getClientId() : null,
            keranjang.getSessionId(),
            items,
            totalHarga
        );
    }

    default DetailKeranjangResponse toDetailResponse(DetailKeranjang detail) {
        if (detail == null) return null;

        Menu menu = detail.getMenu();
        BigDecimal discountedPrice = getDiscountedPrice(menu);
        BigDecimal subtotal = discountedPrice.multiply(BigDecimal.valueOf(detail.getQuantity()));

        return new DetailKeranjangResponse(
            detail.getDetailKeranjangId(),
            menu.getMenuId(),
            menu.getMenuName(),
            discountedPrice,
            menu.getImageUrl(),
            detail.getQuantity(),
            detail.getCatatan(),
            subtotal
        );
    }

    default BigDecimal getDiscountedPrice(Menu menu) {
        if (menu.getPromo() == null || !menu.getPromo().isActive()) {
            return menu.getPrice();
        }
        LocalDate today = LocalDate.now();
        Promo promo = menu.getPromo();
        if (today.isBefore(promo.getTanggalMulai()) || today.isAfter(promo.getTanggalSelesai())) {
            return menu.getPrice();
        }

        if (promo.getTipeDiskon() == TipeDiskon.NOMINAL) {
            BigDecimal discounted = menu.getPrice().subtract(promo.getNilaiDiskon());
            return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
        } else if (promo.getTipeDiskon() == TipeDiskon.PERSEN) {
            BigDecimal discountAmount = menu.getPrice().multiply(promo.getNilaiDiskon()).divide(BigDecimal.valueOf(100));
            BigDecimal discounted = menu.getPrice().subtract(discountAmount);
            return discounted.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : discounted;
        }
        return menu.getPrice();
    }
}
