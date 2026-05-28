package com.aromasenja.domain.keranjang;

import com.aromasenja.domain.keranjang.dto.*;
import com.aromasenja.common.security.UserPrincipal;
import java.util.UUID;

public interface KeranjangService {
    KeranjangResponse getKeranjang(UserPrincipal currentUser);
    KeranjangResponse addItem(AddKeranjangItemRequest request, UserPrincipal currentUser);
    KeranjangResponse updateItem(UUID detailId, UpdateKeranjangItemRequest request, UserPrincipal currentUser);
    KeranjangResponse removeItem(UUID detailId, UserPrincipal currentUser);
    void clearKeranjang(UserPrincipal currentUser);
}
