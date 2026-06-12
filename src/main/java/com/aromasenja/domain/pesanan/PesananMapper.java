package com.aromasenja.domain.pesanan;

import com.aromasenja.domain.pesanan.dto.DetailPesananResponse;
import com.aromasenja.domain.pesanan.dto.PesananResponse;
import com.aromasenja.domain.pesanan.dto.StrukPesananResponse;
import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PesananMapper {

    @Mapping(target = "nomorMeja", source = "meja.nomorMeja", conditionExpression = "java(pesanan.getMeja() != null)")
    @Mapping(target = "mejaId", source = "meja.mejaId", conditionExpression = "java(pesanan.getMeja() != null)")
    @Mapping(target = "clientId", source = "client.clientId", conditionExpression = "java(pesanan.getClient() != null)")
    PesananResponse toResponse(Pesanan pesanan);

    List<PesananResponse> toResponseList(List<Pesanan> pesananList);

    @Mapping(target = "menuId", source = "menu.menuId")
    @Mapping(target = "menuName", source = "menu.menuName")
    @Mapping(target = "imageUrl", source = "menu.imageUrl")
    DetailPesananResponse toDetailResponse(DetailPesanan detail);

    List<DetailPesananResponse> toDetailResponseList(List<DetailPesanan> details);

    @Mapping(target = "menuName", source = "menu.menuName")
    StrukPesananResponse.StrukItem toStrukItem(DetailPesanan detail);

    List<StrukPesananResponse.StrukItem> toStrukItemList(List<DetailPesanan> details);
}
