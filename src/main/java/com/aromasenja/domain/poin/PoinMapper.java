package com.aromasenja.domain.poin;

import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import com.aromasenja.domain.poin.entity.PoinTransaksi;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PoinMapper {

    @Mapping(target = "pesananId", source = "pesanan.pesananId")
    @Mapping(target = "kodePesanan", source = "pesanan.kodePesanan")
    @Mapping(target = "tipe", expression = "java(poinTransaksi.getTipe().toDbValue())")
    PoinRiwayatResponse toResponse(PoinTransaksi poinTransaksi);
}
