package com.aromasenja.domain.pesanan.dto;

import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PesananResponse(
    UUID pesananId,
    String kodePesanan,
    LocalDateTime tanggalPesanan,
    boolean isServed,
    BigDecimal totalHarga,
    BigDecimal jumlahDibayar,
    StatusPesanan status,
    String catatanDapur,
    Integer estimasiMenit,
    MetodePembayaran metodePembayaran,
    Integer poinDigunakan,
    BigDecimal potonganPoin,
    Integer nomorMeja,
    UUID mejaId,
    UUID clientId,
    List<DetailPesananResponse> detailPesanan
) {}
