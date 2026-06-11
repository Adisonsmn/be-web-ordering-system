package com.aromasenja.domain.pesanan;

import com.aromasenja.common.exception.GlobalExceptionHandler;
import com.aromasenja.common.security.JwtService;
import com.aromasenja.domain.pesanan.dto.*;
import com.aromasenja.domain.pesanan.entity.MetodePembayaran;
import com.aromasenja.domain.pesanan.entity.StatusPesanan;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PesananController.class)
@Import({GlobalExceptionHandler.class, com.aromasenja.config.SecurityConfig.class})
@DisplayName("PesananController MockMvc Tests")
class PesananControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private PesananService pesananService;
    @MockBean private JwtService jwtService;
    @MockBean private com.aromasenja.config.CorsConfig corsConfig;

    private UUID pesananId;
    private UUID mejaId;
    private PesananResponse mockResponse;
    private StrukPesananResponse mockStrukResponse;

    @BeforeEach
    void setUp() {
        pesananId = UUID.randomUUID();
        mejaId = UUID.randomUUID();

        mockResponse = new PesananResponse(
                pesananId, "AR-20260528-1111", LocalDateTime.now(), false,
                BigDecimal.valueOf(40000), null, StatusPesanan.NEW, null, 0, null, 10,
                BigDecimal.valueOf(10000), 5, mejaId, UUID.randomUUID(), Collections.emptyList()
        );

        mockStrukResponse = new StrukPesananResponse(
                pesananId, "AR-20260528-1111", LocalDateTime.now(), 5, MetodePembayaran.QRIS,
                BigDecimal.valueOf(50000), BigDecimal.valueOf(10000), BigDecimal.ZERO,
                BigDecimal.valueOf(40000), List.of(new StrukPesananResponse.StrukItem("Nasi Goreng", 2, BigDecimal.valueOf(25000), BigDecimal.valueOf(50000), "Ekstra pedas"))
        );
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("POST /api/pesanan — 201 Created untuk Client")
    void create_201_client() throws Exception {
        CreatePesananRequest request = new CreatePesananRequest(mejaId, "Sangat pedas", true);
        when(pesananService.createPesanan(any(CreatePesananRequest.class), any())).thenReturn(mockResponse);

        mockMvc.perform(post("/api/pesanan")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.kodePesanan").value("AR-20260528-1111"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/pesanan/{id} — 200 OK untuk Client")
    void getDetail_200_client() throws Exception {
        when(pesananService.getPesananDetail(eq(pesananId), any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/pesanan/{pesananId}", pesananId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.kodePesanan").value("AR-20260528-1111"));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/pesanan/{id}/struk — 200 OK untuk Client")
    void getStruk_200_client() throws Exception {
        when(pesananService.getStruk(eq(pesananId), any())).thenReturn(mockStrukResponse);

        mockMvc.perform(get("/api/pesanan/{pesananId}/struk", pesananId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalAkhir").value(40000));
    }

    @Test
    @WithMockUser(roles = "CLIENT")
    @DisplayName("GET /api/pesanan/riwayat — 200 OK untuk Client")
    void getRiwayat_200_client() throws Exception {
        when(pesananService.getRiwayatPesanan(any(), any())).thenReturn(new PageImpl<>(List.of(mockResponse)));

        mockMvc.perform(get("/api/pesanan/riwayat")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/pesanan — 200 OK untuk Admin")
    void getAllAdmin_200_admin() throws Exception {
        when(pesananService.getAllPesananAdmin(any(), any(), any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(mockResponse)));

        mockMvc.perform(get("/api/pesanan")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/pesanan/{id}/status — 200 OK untuk Admin")
    void updateStatus_200_admin() throws Exception {
        UpdateStatusPesananRequest request = new UpdateStatusPesananRequest(StatusPesanan.PREPARING, 15);
        mockResponse = new PesananResponse(
                pesananId, "AR-20260528-1111", LocalDateTime.now(), false,
                BigDecimal.valueOf(40000), null, StatusPesanan.PREPARING, null, 15, null, 10,
                BigDecimal.valueOf(10000), 5, mejaId, UUID.randomUUID(), Collections.emptyList()
        );
        when(pesananService.updateStatus(eq(pesananId), any(UpdateStatusPesananRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/pesanan/{pesananId}/status", pesananId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PREPARING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/pesanan/{id}/bayar — 200 OK untuk Admin")
    void bayar_200_admin() throws Exception {
        BayarPesananRequest request = new BayarPesananRequest(MetodePembayaran.QRIS, BigDecimal.valueOf(40000));
        mockResponse = new PesananResponse(
                pesananId, "AR-20260528-1111", LocalDateTime.now(), true,
                BigDecimal.valueOf(40000), BigDecimal.valueOf(40000), StatusPesanan.SERVED, null, 0, MetodePembayaran.QRIS, 10,
                BigDecimal.valueOf(10000), 5, mejaId, UUID.randomUUID(), Collections.emptyList()
        );
        when(pesananService.bayarPesanan(eq(pesananId), any(BayarPesananRequest.class))).thenReturn(mockResponse);

        mockMvc.perform(patch("/api/pesanan/{pesananId}/bayar", pesananId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.metodePembayaran").value("QRIS"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/pesanan/{id}/cancel — 200 OK untuk Admin")
    void cancel_200_admin() throws Exception {
        doNothing().when(pesananService).cancelPesanan(pesananId);

        mockMvc.perform(patch("/api/pesanan/{pesananId}/cancel", pesananId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
