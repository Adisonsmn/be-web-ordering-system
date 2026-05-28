package com.aromasenja.domain.rating;

import com.aromasenja.domain.rating.dto.RatingResponse;
import com.aromasenja.domain.rating.entity.Rating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RatingMapper {

    @Mapping(target = "clientId", source = "client.clientId")
    @Mapping(target = "clientName", source = "client.user.name")
    @Mapping(target = "menuId", source = "menu.menuId")
    @Mapping(target = "pesananId", source = "pesanan.pesananId")
    RatingResponse toResponse(Rating rating);
}
