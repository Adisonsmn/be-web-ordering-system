package com.aromasenja.domain.poin;

import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.poin.dto.PoinRiwayatResponse;
import com.aromasenja.domain.poin.entity.PoinTransaksi;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:02:38+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class PoinMapperImpl implements PoinMapper {

    @Override
    public PoinRiwayatResponse toResponse(PoinTransaksi poinTransaksi) {
        if ( poinTransaksi == null ) {
            return null;
        }

        UUID pesananId = null;
        String kodePesanan = null;
        UUID poinTransaksiId = null;
        Integer jumlahPoin = null;
        LocalDateTime createdAt = null;

        pesananId = poinTransaksiPesananPesananId( poinTransaksi );
        kodePesanan = poinTransaksiPesananKodePesanan( poinTransaksi );
        poinTransaksiId = poinTransaksi.getPoinTransaksiId();
        jumlahPoin = poinTransaksi.getJumlahPoin();
        createdAt = poinTransaksi.getCreatedAt();

        String tipe = poinTransaksi.getTipe().toDbValue();

        PoinRiwayatResponse poinRiwayatResponse = new PoinRiwayatResponse( poinTransaksiId, pesananId, kodePesanan, jumlahPoin, tipe, createdAt );

        return poinRiwayatResponse;
    }

    private UUID poinTransaksiPesananPesananId(PoinTransaksi poinTransaksi) {
        Pesanan pesanan = poinTransaksi.getPesanan();
        if ( pesanan == null ) {
            return null;
        }
        return pesanan.getPesananId();
    }

    private String poinTransaksiPesananKodePesanan(PoinTransaksi poinTransaksi) {
        Pesanan pesanan = poinTransaksi.getPesanan();
        if ( pesanan == null ) {
            return null;
        }
        return pesanan.getKodePesanan();
    }
}
