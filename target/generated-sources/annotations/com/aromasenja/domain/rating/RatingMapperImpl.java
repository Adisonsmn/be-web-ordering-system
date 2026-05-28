package com.aromasenja.domain.rating;

import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.rating.dto.RatingResponse;
import com.aromasenja.domain.rating.entity.Rating;
import com.aromasenja.domain.user.entity.Client;
import com.aromasenja.domain.user.entity.User;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:02:40+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class RatingMapperImpl implements RatingMapper {

    @Override
    public RatingResponse toResponse(Rating rating) {
        if ( rating == null ) {
            return null;
        }

        UUID clientId = null;
        String clientName = null;
        UUID menuId = null;
        UUID pesananId = null;
        UUID ratingId = null;
        Short bintang = null;
        String ulasan = null;
        LocalDateTime createdAt = null;

        clientId = ratingClientClientId( rating );
        clientName = ratingClientUserName( rating );
        menuId = ratingMenuMenuId( rating );
        pesananId = ratingPesananPesananId( rating );
        ratingId = rating.getRatingId();
        bintang = rating.getBintang();
        ulasan = rating.getUlasan();
        createdAt = rating.getCreatedAt();

        boolean isOverall = false;
        boolean isPublic = false;

        RatingResponse ratingResponse = new RatingResponse( ratingId, clientId, clientName, menuId, pesananId, bintang, ulasan, isOverall, isPublic, createdAt );

        return ratingResponse;
    }

    private UUID ratingClientClientId(Rating rating) {
        Client client = rating.getClient();
        if ( client == null ) {
            return null;
        }
        return client.getClientId();
    }

    private String ratingClientUserName(Rating rating) {
        Client client = rating.getClient();
        if ( client == null ) {
            return null;
        }
        User user = client.getUser();
        if ( user == null ) {
            return null;
        }
        return user.getName();
    }

    private UUID ratingMenuMenuId(Rating rating) {
        Menu menu = rating.getMenu();
        if ( menu == null ) {
            return null;
        }
        return menu.getMenuId();
    }

    private UUID ratingPesananPesananId(Rating rating) {
        Pesanan pesanan = rating.getPesanan();
        if ( pesanan == null ) {
            return null;
        }
        return pesanan.getPesananId();
    }
}
