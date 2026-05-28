package com.aromasenja.domain.meja;

import com.aromasenja.domain.meja.dto.MejaResponse;
import com.aromasenja.domain.meja.entity.Meja;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:02:39+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class MejaMapperImpl implements MejaMapper {

    @Override
    public MejaResponse toResponse(Meja meja) {
        if ( meja == null ) {
            return null;
        }

        UUID mejaId = null;
        Integer nomorMeja = null;
        String qrCodeUrl = null;

        mejaId = meja.getMejaId();
        nomorMeja = meja.getNomorMeja();
        qrCodeUrl = meja.getQrCodeUrl();

        String zone = meja.getZone() != null ? meja.getZone().name() : null;
        boolean isActive = false;
        boolean isOccupied = false;

        MejaResponse mejaResponse = new MejaResponse( mejaId, nomorMeja, zone, isActive, isOccupied, qrCodeUrl );

        return mejaResponse;
    }

    @Override
    public List<MejaResponse> toResponseList(List<Meja> mejaList) {
        if ( mejaList == null ) {
            return null;
        }

        List<MejaResponse> list = new ArrayList<MejaResponse>( mejaList.size() );
        for ( Meja meja : mejaList ) {
            list.add( toResponse( meja ) );
        }

        return list;
    }
}
