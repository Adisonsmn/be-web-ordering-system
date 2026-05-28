package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConfigMapper {
    RestoConfigResponse toResponse(RestoConfig restoConfig);
}
