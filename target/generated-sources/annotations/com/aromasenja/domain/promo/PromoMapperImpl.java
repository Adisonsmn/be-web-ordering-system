package com.aromasenja.domain.promo;

import com.aromasenja.domain.promo.dto.CreatePromoRequest;
import com.aromasenja.domain.promo.dto.PromoResponse;
import com.aromasenja.domain.promo.entity.Promo;
import com.aromasenja.domain.promo.entity.TipeDiskon;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:02:37+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class PromoMapperImpl implements PromoMapper {

    @Override
    public PromoResponse toResponse(Promo promo) {
        if ( promo == null ) {
            return null;
        }

        UUID promoId = null;
        String namaPromo = null;
        TipeDiskon tipeDiskon = null;
        BigDecimal nilaiDiskon = null;
        LocalDate tanggalMulai = null;
        LocalDate tanggalSelesai = null;
        String targetCategory = null;
        String imageUrl = null;
        String tag = null;
        String description = null;

        promoId = promo.getPromoId();
        namaPromo = promo.getNamaPromo();
        tipeDiskon = promo.getTipeDiskon();
        nilaiDiskon = promo.getNilaiDiskon();
        tanggalMulai = promo.getTanggalMulai();
        tanggalSelesai = promo.getTanggalSelesai();
        targetCategory = promo.getTargetCategory();
        imageUrl = promo.getImageUrl();
        tag = promo.getTag();
        description = promo.getDescription();

        boolean isActive = false;

        PromoResponse promoResponse = new PromoResponse( promoId, namaPromo, tipeDiskon, nilaiDiskon, tanggalMulai, tanggalSelesai, targetCategory, isActive, imageUrl, tag, description );

        return promoResponse;
    }

    @Override
    public List<PromoResponse> toResponseList(List<Promo> promos) {
        if ( promos == null ) {
            return null;
        }

        List<PromoResponse> list = new ArrayList<PromoResponse>( promos.size() );
        for ( Promo promo : promos ) {
            list.add( toResponse( promo ) );
        }

        return list;
    }

    @Override
    public Promo toEntity(CreatePromoRequest request) {
        if ( request == null ) {
            return null;
        }

        Promo promo = new Promo();

        promo.setNamaPromo( request.namaPromo() );
        promo.setTipeDiskon( request.tipeDiskon() );
        promo.setNilaiDiskon( request.nilaiDiskon() );
        promo.setTanggalMulai( request.tanggalMulai() );
        promo.setTanggalSelesai( request.tanggalSelesai() );
        promo.setTargetCategory( request.targetCategory() );
        promo.setImageUrl( request.imageUrl() );
        promo.setTag( request.tag() );
        promo.setDescription( request.description() );

        return promo;
    }

    @Override
    public void updateEntityFromRequest(CreatePromoRequest request, Promo promo) {
        if ( request == null ) {
            return;
        }

        promo.setNamaPromo( request.namaPromo() );
        promo.setTipeDiskon( request.tipeDiskon() );
        promo.setNilaiDiskon( request.nilaiDiskon() );
        promo.setTanggalMulai( request.tanggalMulai() );
        promo.setTanggalSelesai( request.tanggalSelesai() );
        promo.setTargetCategory( request.targetCategory() );
        promo.setImageUrl( request.imageUrl() );
        promo.setTag( request.tag() );
        promo.setDescription( request.description() );
    }
}
