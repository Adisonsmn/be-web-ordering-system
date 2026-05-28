package com.aromasenja.domain.meja;

import com.aromasenja.domain.meja.dto.MejaResponse;
import com.aromasenja.domain.meja.entity.Meja;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MejaMapper {

    @Mapping(target = "zone", expression = "java(meja.getZone() != null ? meja.getZone().name() : null)")
    MejaResponse toResponse(Meja meja);

    List<MejaResponse> toResponseList(List<Meja> mejaList);
}
