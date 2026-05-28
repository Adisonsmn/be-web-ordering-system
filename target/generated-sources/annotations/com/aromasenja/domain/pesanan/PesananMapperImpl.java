package com.aromasenja.domain.pesanan;

import com.aromasenja.domain.meja.entity.Meja;
import com.aromasenja.domain.menu.entity.Menu;
import com.aromasenja.domain.pesanan.dto.DetailPesananResponse;
import com.aromasenja.domain.pesanan.dto.PesananResponse;
import com.aromasenja.domain.pesanan.dto.StrukPesananResponse;
import com.aromasenja.domain.pesanan.entity.DetailPesanan;
import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import com.aromasenja.domain.pesanan.entity.Pesanan;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.aromasenja.domain.user.entity.Client;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-28T15:02:38+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.11 (Microsoft)"
)
@Component
public class PesananMapperImpl implements PesananMapper {

    @Override
    public PesananResponse toResponse(Pesanan pesanan) {
        if ( pesanan == null ) {
            return null;
        }

        Integer nomorMeja = null;
        UUID mejaId = null;
        UUID clientId = null;
        UUID pesananId = null;
        String kodePesanan = null;
        LocalDateTime tanggalPesanan = null;
        BigDecimal totalHarga = null;
        BigDecimal jumlahDibayar = null;
        StatusPesanan status = null;
        String catatanDapur = null;
        Integer estimasiMenit = null;
        MetodePembayaran metodePembayaran = null;
        Integer poinDigunakan = null;
        BigDecimal potonganPoin = null;
        List<DetailPesananResponse> detailPesanan = null;

        Integer nomorMeja1 = pesananMejaNomorMeja( pesanan );
        if ( pesanan.getMeja() != null ) {
            nomorMeja = nomorMeja1;
        }
        UUID mejaId1 = pesananMejaMejaId( pesanan );
        if ( pesanan.getMeja() != null ) {
            mejaId = mejaId1;
        }
        UUID clientId1 = pesananClientClientId( pesanan );
        if ( pesanan.getClient() != null ) {
            clientId = clientId1;
        }
        pesananId = pesanan.getPesananId();
        kodePesanan = pesanan.getKodePesanan();
        tanggalPesanan = pesanan.getTanggalPesanan();
        totalHarga = pesanan.getTotalHarga();
        jumlahDibayar = pesanan.getJumlahDibayar();
        status = pesanan.getStatus();
        catatanDapur = pesanan.getCatatanDapur();
        estimasiMenit = pesanan.getEstimasiMenit();
        metodePembayaran = pesanan.getMetodePembayaran();
        poinDigunakan = pesanan.getPoinDigunakan();
        potonganPoin = pesanan.getPotonganPoin();
        detailPesanan = toDetailResponseList( pesanan.getDetailPesanan() );

        boolean isServed = false;

        PesananResponse pesananResponse = new PesananResponse( pesananId, kodePesanan, tanggalPesanan, isServed, totalHarga, jumlahDibayar, status, catatanDapur, estimasiMenit, metodePembayaran, poinDigunakan, potonganPoin, nomorMeja, mejaId, clientId, detailPesanan );

        return pesananResponse;
    }

    @Override
    public List<PesananResponse> toResponseList(List<Pesanan> pesananList) {
        if ( pesananList == null ) {
            return null;
        }

        List<PesananResponse> list = new ArrayList<PesananResponse>( pesananList.size() );
        for ( Pesanan pesanan : pesananList ) {
            list.add( toResponse( pesanan ) );
        }

        return list;
    }

    @Override
    public DetailPesananResponse toDetailResponse(DetailPesanan detail) {
        if ( detail == null ) {
            return null;
        }

        UUID menuId = null;
        String menuName = null;
        String imageUrl = null;
        UUID detailPesananId = null;
        Integer quantity = null;
        String catatan = null;
        BigDecimal hargaSnapshot = null;
        BigDecimal hargaSetelahDiskon = null;
        BigDecimal subTotal = null;

        menuId = detailMenuMenuId( detail );
        menuName = detailMenuMenuName( detail );
        imageUrl = detailMenuImageUrl( detail );
        detailPesananId = detail.getDetailPesananId();
        quantity = detail.getQuantity();
        catatan = detail.getCatatan();
        hargaSnapshot = detail.getHargaSnapshot();
        hargaSetelahDiskon = detail.getHargaSetelahDiskon();
        subTotal = detail.getSubTotal();

        DetailPesananResponse detailPesananResponse = new DetailPesananResponse( detailPesananId, menuId, menuName, imageUrl, quantity, catatan, hargaSnapshot, hargaSetelahDiskon, subTotal );

        return detailPesananResponse;
    }

    @Override
    public List<DetailPesananResponse> toDetailResponseList(List<DetailPesanan> details) {
        if ( details == null ) {
            return null;
        }

        List<DetailPesananResponse> list = new ArrayList<DetailPesananResponse>( details.size() );
        for ( DetailPesanan detailPesanan : details ) {
            list.add( toDetailResponse( detailPesanan ) );
        }

        return list;
    }

    @Override
    public StrukPesananResponse.StrukItem toStrukItem(DetailPesanan detail) {
        if ( detail == null ) {
            return null;
        }

        String menuName = null;
        Integer quantity = null;
        BigDecimal hargaSetelahDiskon = null;
        BigDecimal subTotal = null;

        menuName = detailMenuMenuName( detail );
        quantity = detail.getQuantity();
        hargaSetelahDiskon = detail.getHargaSetelahDiskon();
        subTotal = detail.getSubTotal();

        StrukPesananResponse.StrukItem strukItem = new StrukPesananResponse.StrukItem( menuName, quantity, hargaSetelahDiskon, subTotal );

        return strukItem;
    }

    @Override
    public List<StrukPesananResponse.StrukItem> toStrukItemList(List<DetailPesanan> details) {
        if ( details == null ) {
            return null;
        }

        List<StrukPesananResponse.StrukItem> list = new ArrayList<StrukPesananResponse.StrukItem>( details.size() );
        for ( DetailPesanan detailPesanan : details ) {
            list.add( toStrukItem( detailPesanan ) );
        }

        return list;
    }

    private Integer pesananMejaNomorMeja(Pesanan pesanan) {
        Meja meja = pesanan.getMeja();
        if ( meja == null ) {
            return null;
        }
        return meja.getNomorMeja();
    }

    private UUID pesananMejaMejaId(Pesanan pesanan) {
        Meja meja = pesanan.getMeja();
        if ( meja == null ) {
            return null;
        }
        return meja.getMejaId();
    }

    private UUID pesananClientClientId(Pesanan pesanan) {
        Client client = pesanan.getClient();
        if ( client == null ) {
            return null;
        }
        return client.getClientId();
    }

    private UUID detailMenuMenuId(DetailPesanan detailPesanan) {
        Menu menu = detailPesanan.getMenu();
        if ( menu == null ) {
            return null;
        }
        return menu.getMenuId();
    }

    private String detailMenuMenuName(DetailPesanan detailPesanan) {
        Menu menu = detailPesanan.getMenu();
        if ( menu == null ) {
            return null;
        }
        return menu.getMenuName();
    }

    private String detailMenuImageUrl(DetailPesanan detailPesanan) {
        Menu menu = detailPesanan.getMenu();
        if ( menu == null ) {
            return null;
        }
        return menu.getImageUrl();
    }
}
