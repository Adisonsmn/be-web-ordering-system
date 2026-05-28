package com.aromasenja.domain.poin;

import com.aromasenja.common.exception.BusinessException;
import com.aromasenja.common.exception.UnauthorizedException;
import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.poin.dto.PoinBalanceResponse;
import com.aromasenja.domain.poin.dto.PoinKalkulasiRequest;
import com.aromasenja.domain.poin.dto.PoinKalkulasiResponse;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import com.aromasenja.domain.poin.entity.PoinTransaksi;
import com.aromasenja.domain.user.ClientRepository;
import com.aromasenja.domain.user.entity.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PoinServiceImpl implements PoinService {

    private final PoinTransaksiRepository poinTransaksiRepository;
    private final ClientRepository clientRepository;
    private final PoinMapper poinMapper;

    @Value("${app.poin.rupiah-per-poin}")
    private Integer rupiahPerPoin;

    @Override
    @Transactional(readOnly = true)
    public PoinBalanceResponse getPoinBalance(UserPrincipal currentUser) {
        if (currentUser.isGuest()) {
            throw new UnauthorizedException("Guest tidak memiliki poin loyalitas");
        }

        Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));

        return new PoinBalanceResponse(client.getTotalPoint(), rupiahPerPoin);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PoinRiwayatResponse> getRiwayatPoin(UserPrincipal currentUser, Pageable pageable) {
        if (currentUser.isGuest()) {
            throw new UnauthorizedException("Guest tidak memiliki riwayat transaksi poin");
        }

        Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));

        Page<PoinTransaksi> page = poinTransaksiRepository.findByClientClientIdOrderByCreatedAtDesc(client.getClientId(), pageable);
        return page.map(poinMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PoinKalkulasiResponse kalkulasiPoin(PoinKalkulasiRequest request, UserPrincipal currentUser) {
        if (currentUser.isGuest()) {
            throw new UnauthorizedException("Guest tidak dapat menggunakan poin");
        }

        Client client = clientRepository.findByUser_Id(currentUser.getUserId())
                .orElseThrow(() -> new BusinessException("Profil client tidak ditemukan"));

        if (request.poinDigunakan() > client.getTotalPoint()) {
            throw new BusinessException("Saldo poin tidak mencukupi");
        }

        BigDecimal discountPerPoin = BigDecimal.valueOf(rupiahPerPoin);
        BigDecimal potentialDiscount = BigDecimal.valueOf(request.poinDigunakan()).multiply(discountPerPoin);
        BigDecimal diskonRupiah = potentialDiscount.min(request.pesananSubtotal());
        BigDecimal totalSetelahDiskon = request.pesananSubtotal().subtract(diskonRupiah);

        return new PoinKalkulasiResponse(diskonRupiah, totalSetelahDiskon);
    }
}
