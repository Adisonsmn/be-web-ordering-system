package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConfigMapper {
    @Mapping(source = "open", target = "isOpen")
    RestoConfigResponse toResponse(RestoConfig restoConfig);
}
