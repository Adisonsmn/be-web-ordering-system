package com.aromasenja.domain.poin;

import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.poin.dto.PoinBalanceResponse;
import com.aromasenja.domain.poin.dto.PoinKalkulasiRequest;
import com.aromasenja.domain.poin.dto.PoinKalkulasiResponse;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PoinService {

    PoinBalanceResponse getPoinBalance(UserPrincipal currentUser);

    Page<PoinRiwayatResponse> getRiwayatPoin(UserPrincipal currentUser, Pageable pageable);

    PoinKalkulasiResponse kalkulasiPoin(PoinKalkulasiRequest request, UserPrincipal currentUser);
}
