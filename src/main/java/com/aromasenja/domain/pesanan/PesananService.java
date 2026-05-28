package com.aromasenja.domain.pesanan;

import com.aromasenja.common.security.UserPrincipal;
import com.aromasenja.domain.pesanan.dto.*;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.UUID;

public interface PesananService {

    PesananResponse createPesanan(CreatePesananRequest request, UserPrincipal currentUser);

    PesananResponse getPesananDetail(UUID pesananId, UserPrincipal currentUser);

    StrukPesananResponse getStruk(UUID pesananId, UserPrincipal currentUser);

    Page<PesananResponse> getRiwayatPesanan(UserPrincipal currentUser, Pageable pageable);

    Page<PesananResponse> getAllPesananAdmin(StatusPesanan status, UUID mejaId, LocalDate tanggal, Pageable pageable);

    PesananResponse updateStatus(UUID pesananId, UpdateStatusPesananRequest request);

    PesananResponse bayarPesanan(UUID pesananId, BayarPesananRequest request);

    void cancelPesanan(UUID pesananId);
}
