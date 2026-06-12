package com.aromasenja.domain.promo;

import com.aromasenja.domain.promo.dto.CreatePromoRequest;
import com.aromasenja.domain.promo.dto.PromoResponse;
import com.aromasenja.domain.promo.entity.Promo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PromoMapper {

    // MapStruct strip prefix 'is' dari boolean getter: isActive() → property 'active'
    // Tanpa @Mapping eksplisit ini, field isActive selalu false di response
    @Mapping(source = "active", target = "isActive")
    PromoResponse toResponse(Promo promo);

    @Mapping(source = "active", target = "isActive")
    List<PromoResponse> toResponseList(List<Promo> promos);

    @Mapping(target = "promoId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    Promo toEntity(CreatePromoRequest request);

    @Mapping(target = "promoId", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "usageCount", ignore = true)
    void updateEntityFromRequest(CreatePromoRequest request, @MappingTarget Promo promo);
}
