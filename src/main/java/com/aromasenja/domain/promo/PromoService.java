package com.aromasenja.domain.promo;

import com.aromasenja.domain.promo.dto.CreatePromoRequest;
import com.aromasenja.domain.promo.dto.PromoResponse;
import java.util.List;
import java.util.UUID;

public interface PromoService {

    List<PromoResponse> getActivePromosForClient();

    List<PromoResponse> getAllPromosForAdmin(String status);

    PromoResponse createPromo(CreatePromoRequest request);

    PromoResponse updatePromo(UUID promoId, CreatePromoRequest request);

    void deletePromo(UUID promoId);
}
