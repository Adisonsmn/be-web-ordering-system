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

    PromoResponse toResponse(Promo promo);

    List<PromoResponse> toResponseList(List<Promo> promos);

    @Mapping(target = "promoId", ignore = true)
    @Mapping(target = "active", ignore = true)
    Promo toEntity(CreatePromoRequest request);

    @Mapping(target = "promoId", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(CreatePromoRequest request, @MappingTarget Promo promo);
}
