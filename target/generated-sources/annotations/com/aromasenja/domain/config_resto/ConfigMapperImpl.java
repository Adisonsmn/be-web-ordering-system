package com.aromasenja.domain.config_resto;

import com.aromasenja.domain.config_resto.dto.RestoConfigResponse;
import com.aromasenja.domain.config_resto.entity.RestoConfig;
import java.time.LocalTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:02:39+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class ConfigMapperImpl implements ConfigMapper {

    @Override
    public RestoConfigResponse toResponse(RestoConfig restoConfig) {
        if ( restoConfig == null ) {
            return null;
        }

        LocalTime openTime = null;
        LocalTime closeTime = null;
        String namaRestoran = null;
        String tagline = null;
        String alamat = null;
        String telepon = null;
        String email = null;
        String instagram = null;

        openTime = restoConfig.getOpenTime();
        closeTime = restoConfig.getCloseTime();
        namaRestoran = restoConfig.getNamaRestoran();
        tagline = restoConfig.getTagline();
        alamat = restoConfig.getAlamat();
        telepon = restoConfig.getTelepon();
        email = restoConfig.getEmail();
        instagram = restoConfig.getInstagram();

        boolean isOpen = false;

        RestoConfigResponse restoConfigResponse = new RestoConfigResponse( isOpen, openTime, closeTime, namaRestoran, tagline, alamat, telepon, email, instagram );

        return restoConfigResponse;
    }
}
